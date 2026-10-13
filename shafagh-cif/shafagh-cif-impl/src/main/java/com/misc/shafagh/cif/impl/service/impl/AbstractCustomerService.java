package com.misc.shafagh.cif.impl.service.impl;

import com.misc.shafagh.cif.impl.dto.mapper.CustomerMapper;
import com.misc.shafagh.cif.impl.dto.request.CustomerFilterDTO;
import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.cif.impl.entity.CustomerAddressEntity;
import com.misc.shafagh.cif.impl.entity.CustomerContactEntity;
import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import com.misc.shafagh.cif.impl.entity.enums.CustomerStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import com.misc.shafagh.cif.impl.entity.validator.CustomerValidator;
import com.misc.shafagh.cif.impl.exception.InvalidPersonTypeException;
import com.misc.shafagh.cif.impl.exception.ValidationException;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.cif.impl.repository.CustomerRepository;
import com.misc.shafagh.cif.impl.repository.CustomerSpecification;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * منطق مشترک سرویس‌های اشخاص حقیقی و حقوقی.
 * <p>
 * کلاس‌های فرزند فقط باید این موارد را مشخص کنند:
 * <ul>
 *     <li>{@link #allowedTypes()} : انواع مجاز شخص</li>
 *     <li>{@link #personLabel()} : برچسب برای پیام‌های خطا (حقیقی / حقوقی)</li>
 *     <li>{@link #validateSpecific(CustomerReqDTO)} : اعتبارسنجی مخصوص همان نوع</li>
 *     <li>{@link #beforeSave(CustomerReqDTO)} : (اختیاری) مقداردهی پیش‌فرض مخصوص ایجاد</li>
 * </ul>
 */
@Transactional("cifTransactionManager")
public abstract class AbstractCustomerService {

    @org.springframework.beans.factory.annotation.Value("${shafagh.fund-id:1}")
    private long fundId;

    protected final CustomerRepository personRepo;
    protected final CityService cityService;
    protected final CustomerMapper customerMapper;
    protected final CustomerValidator customerValidator;
    protected final CustomerSignatureHelper signatureHelper;

    protected AbstractCustomerService(CustomerRepository personRepo,
                                      CityService cityService,
                                      CustomerMapper customerMapper,
                                      CustomerValidator customerValidator,
                                      CustomerSignatureHelper signatureHelper) {
        this.personRepo = personRepo;
        this.cityService = cityService;
        this.customerMapper = customerMapper;
        this.customerValidator = customerValidator;
        this.signatureHelper = signatureHelper;
    }

    // ------------------------------------------------------------------
    // نقاط توسعه برای کلاس‌های فرزند
    // ------------------------------------------------------------------

    protected abstract Set<CustomerType> allowedTypes();

    /** مثلا «حقیقی» یا «حقوقی» */
    protected abstract String personLabel();

    /** اعتبارسنجی مخصوص نوع شخص (validateReal / validateLegal) */
    protected abstract void validateSpecific(CustomerReqDTO reqDTO);

    /** هوک اختیاری: مقداردهی پیش‌فرض مخصوص ایجاد */
    protected void beforeSave(CustomerReqDTO reqDTO) {
    }

    // ------------------------------------------------------------------
    // عملیات عمومی
    // ------------------------------------------------------------------

    @Transactional(transactionManager = "cifTransactionManager", readOnly = true)
    public Page<CustomerDto> findAll(Pageable pageable) {
        return toDtoPage(personRepo.findAllByCustomerTypeIn(allowedTypes(), pageable));
    }

    @Transactional(transactionManager = "cifTransactionManager", readOnly = true)
    public Page<CustomerDto> search(CustomerFilterDTO filter, Pageable pageable) {
        if (filter != null && filter.getCustomerType() != null) {
            validateType(filter.getCustomerType());
        }
        Specification<CustomerEntity> spec = CustomerSpecification.byFilter(filter, allowedTypes());
        return toDtoPage(personRepo.findAll(spec, pageable));
    }

    public CustomerDto save(CustomerReqDTO reqDTO) {
        requireRequest(reqDTO);
        if (reqDTO.getId() != null) {
            throw new ValidationException("برای ثبت مشتری جدید نباید شناسه ارسال شود");
        }
        validateType(reqDTO.getCustomerType());
        applyCommonDefaults(reqDTO);
        reqDTO.setCustomerCode(generateCustomerCode());
        beforeSave(reqDTO);

        customerValidator.validateCommon(reqDTO);
        validateSpecific(reqDTO);

        if (personRepo.getFirstByIdentifier(reqDTO.getIdentifier()) != null) {
            throw new com.misc.shafagh.cif.impl.exception.DuplicateCustomerIdentifierException();
        }

        return persist(customerMapper.toEntity(reqDTO), reqDTO);
    }

    public CustomerDto update(CustomerReqDTO reqDTO) {
        requireRequest(reqDTO);
        if (reqDTO.getId() == null) {
            throw new ValidationException("شناسه مشتری برای ویرایش الزامی است");
        }
        CustomerEntity entity = findOwnedOrThrow(reqDTO.getId());
        validateType(reqDTO.getCustomerType());
        applyCommonDefaults(reqDTO);

        customerValidator.validateCommon(reqDTO);
        validateSpecific(reqDTO);

        CustomerEntity duplicate = personRepo.getFirstByIdentifier(reqDTO.getIdentifier());
        if (duplicate != null && !duplicate.getId().equals(entity.getId())) {
            throw new com.misc.shafagh.cif.impl.exception.DuplicateCustomerIdentifierException();
        }

        // Important: addresses are deliberately ignored by MapStruct during update.
        // Map the request to a temporary graph, then copy address values onto the
        // managed child entities. This prevents Hibernate from receiving detached
        // CustomerAddressEntity instances that already have database IDs.
        CustomerEntity requestedGraph = customerMapper.toEntity(reqDTO);
        customerMapper.updateEntityFromDto(reqDTO, entity);
        syncAddresses(entity, requestedGraph.getAddresses());
        syncContacts(entity, requestedGraph.getContacts());

        return persist(entity, reqDTO);
    }

    @Transactional(transactionManager = "cifTransactionManager", readOnly = true)
    public CustomerDto getById(long id) {
        CustomerDto dto = customerMapper.toDto(findOwnedOrThrow(id));
        fillProvince(dto, getProvinceMap());
        return dto;
    }

    public void delete(long id) {
        personRepo.delete(findOwnedOrThrow(id));
    }

    // ------------------------------------------------------------------
    // منطق داخلی مشترک
    // ------------------------------------------------------------------

    private CustomerDto persist(CustomerEntity entity, CustomerReqDTO reqDTO) {
        linkChildren(entity);
        resolveCities(entity);
        signatureHelper.sync(entity, reqDTO.getSignatures());

        CustomerEntity saved;
        try {
            saved = personRepo.saveAndFlush(entity);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
                        && violation.getConstraintName() != null) {
                    String name = violation.getConstraintName().toUpperCase(java.util.Locale.ROOT);
                    if (name.contains("UK_CUSTOMER_FUND_ID_IDENTIFIER") || name.contains("UDX_CUSTOMER_FUND_ID_IDENTIFIER"))
                        throw new com.misc.shafagh.cif.impl.exception.DuplicateCustomerIdentifierException();
                }
            }
            throw exception;
        }
        CustomerDto result = customerMapper.toDto(saved);
        fillProvince(result, getProvinceMap());
        return result;
    }

    private void applyCommonDefaults(CustomerReqDTO reqDTO) {
        reqDTO.setFundId(fundId);
        reqDTO.setCustomerStatus(CustomerStatus.ACTIVE);
        forEachSafe(reqDTO.getContacts(), x -> x.setFundId(fundId));
        forEachSafe(reqDTO.getAddresses(), x -> x.setFundId(fundId));
        forEachSafe(reqDTO.getSignatures(), x -> x.setFundId(fundId));
    }

    private void requireRequest(CustomerReqDTO dto) {
        if (dto == null) {
            throw new ValidationException("درخواست نمی‌تواند خالی باشد");
        }
    }

    protected static <T> void forEachSafe(List<T> list, Consumer<T> action) {
        if (list != null) {
            list.forEach(item -> {
                if (item == null) {
                    throw new ValidationException("اطلاعات وابسته نمی‌تواند خالی باشد");
                }
                action.accept(item);
            });
        }
    }

    // TODO: add customer_no
    private String generateCustomerCode() {
        return "C-" + ThreadLocalRandom.current().nextLong(1_000_000_000L);
    }

    protected void validateType(CustomerType type) {
        if (type == null || !allowedTypes().contains(type)) {
            throw new InvalidPersonTypeException(
                    "نوع شخص ارسالی برای سرویس اشخاص " + personLabel() + " معتبر نیست: " + type);
        }
    }

    protected CustomerEntity findOwnedOrThrow(long id) {
        CustomerEntity entity = personRepo.getFirstByIdAndCustomerTypeIn(id, allowedTypes());
        if (entity == null) {
            throw new InvalidPersonTypeException("شخص " + personLabel() + " با شناسه " + id + " یافت نشد");
        }
        return entity;
    }

    private void linkChildren(CustomerEntity entity) {
        entity.getAddresses().forEach(addr -> addr.setCustomer(entity));
        entity.getContacts().forEach(contact -> contact.setCustomer(entity));
    }

    private void syncAddresses(CustomerEntity customer, List<CustomerAddressEntity> requestedAddresses) {
        if (requestedAddresses == null) {
            return;
        }

        Map<Long, CustomerAddressEntity> existingById = customer.getAddresses().stream()
                .filter(address -> address.getId() != null)
                .collect(Collectors.toMap(CustomerAddressEntity::getId, address -> address));

        List<CustomerAddressEntity> synchronizedAddresses = new java.util.ArrayList<>();

        for (CustomerAddressEntity requested : requestedAddresses) {
            CustomerAddressEntity target;

            if (requested.getId() == null) {
                target = new CustomerAddressEntity();
            } else {
                target = existingById.remove(requested.getId());
                if (target == null) {
                    throw new ValidationException(
                            "آدرس با شناسه " + requested.getId() + " متعلق به این شخص نیست");
                }
            }

            target.setFundId(requested.getFundId());
            target.setAddressType(requested.getAddressType());
            target.setAddress(requested.getAddress());
            target.setPostalCode(requested.getPostalCode());

            target.setCityId(requested.getCityId());

            target.setCustomer(customer);
            synchronizedAddresses.add(target);
        }

        // Mutate the Hibernate-managed collection instead of replacing it.
        customer.getAddresses().clear();
        customer.getAddresses().addAll(synchronizedAddresses);
    }


    private void syncContacts(CustomerEntity customer, List<CustomerContactEntity> requestedContacts) {
        if (requestedContacts == null) {
            return;
        }

        Map<Long, CustomerContactEntity> existingById = customer.getContacts().stream()
                .filter(contact -> contact.getId() != null)
                .collect(Collectors.toMap(CustomerContactEntity::getId, contact -> contact));

        List<CustomerContactEntity> synchronizedContacts = new java.util.ArrayList<>();

        for (CustomerContactEntity requested : requestedContacts) {
            CustomerContactEntity target;

            if (requested.getId() == null) {
                target = new CustomerContactEntity();
            } else {
                target = existingById.remove(requested.getId());
                if (target == null) {
                    throw new ValidationException(
                            "اطلاعات تماس با شناسه " + requested.getId() + " متعلق به این شخص نیست");
                }
            }

            // Copy all mapped contact fields, but never overwrite identity/ownership.
            BeanUtils.copyProperties(requested, target, "id", "customer");
            target.setCustomer(customer);
            synchronizedContacts.add(target);
        }

        // Keep Hibernate's managed PersistentCollection instance; orphanRemoval handles removed rows.
        customer.getContacts().clear();
        customer.getContacts().addAll(synchronizedContacts);
    }

    private void resolveCities(CustomerEntity entity) {
        cityService.getById(entity.getCityId());
        entity.getAddresses().forEach(addr -> cityService.getById(addr.getCityId()));
    }

    // ------------------------------------------------------------------
    // استان
    // ------------------------------------------------------------------

    /** نقشه استان‌ها فقط یک بار برای کل صفحه ساخته می‌شود (نه برای هر رکورد) */
    private Page<CustomerDto> toDtoPage(Page<CustomerEntity> entities) {
        Map<Long, String> provinceMap = getProvinceMap();
        return entities.map(entity -> {
            CustomerDto dto = customerMapper.toDto(entity);
            fillProvince(dto, provinceMap);
            return dto;
        });
    }

    private Map<Long, String> getProvinceMap() {
        return cityService.findAllIncludingProvinces().stream()
                .filter(city -> Objects.equals(city.getParentId(), 0L))
                .collect(Collectors.toMap(CityDto::getId, CityDto::getName));
    }

    private void fillProvince(CustomerDto dto, Map<Long, String> provinceMap) {
        if (dto == null) {
            return;
        }
        if (dto.getCity() != null && dto.getCity().getParentId() != null) {
            dto.getCity().setProvince(provinceMap.get(dto.getCity().getParentId()));
        }
        if (dto.getAddresses() != null) {
            dto.getAddresses().forEach(address -> {
                if (address.getCity() != null && address.getCity().getParentId() != null) {
                    address.getCity().setProvince(provinceMap.get(address.getCity().getParentId()));
                }
            });
        }
    }
}
