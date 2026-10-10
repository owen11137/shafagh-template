package com.misc.shafagh.cif.impl.wage;/*
package com.misc.shafagh.core.integration.wage;

import com.misc.shafagh.core.integration.wage.dto.PaymentType;
import com.misc.shafagh.core.integration.wage.dto.WageCalcResponseDto;
import com.misc.shafagh.core.integration.wage.dto.WageCreateRequestDto;
import com.misc.shafagh.core.integration.wage.dto.WageCreateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WageClient {

    //private final TokenClient tokenClient;
    private final WebClient webClient;
    @Value("${external-services.wage-service.estimate}")
    private String wageEstimateUri;
    @Value("${external-services.wage-service.create}")
    private String wageCreateUri;
    @Value("${external-services.wage-service.submit}")
    private String wageSubmitUri;
    @Value("${external-services.wage-service.cancel}")
    private String wageCancelUri;

    public WageCalcResponseDto getWageEstimate(long totalAmount, int totalCount, PaymentType paymentType, String account) {

        return webClient
                .get()
                .uri(wageEstimateUri + "?totalAmount=" + totalAmount + "&totalCount=" + totalCount + "&paymentType=" + paymentType + "&account=" + account)
                .header(HttpHeaders.AUTHORIZATION, "tokenClient.getUserToken()")
                .retrieve()
                .bodyToMono(WageCalcResponseDto.class)
                .onErrorMap(WageClientExceptions::new)
                .block();
    }

    public WageCreateResponseDto createWageRequest(WageCreateRequestDto requestDto) {
        return webClient
                .post()
                .uri(wageCreateUri)
                .header(HttpHeaders.AUTHORIZATION, "tokenClient.getUserToken()")
                .body(Mono.just(requestDto), WageCreateRequestDto.class)
                .retrieve()
                .bodyToMono(WageCreateResponseDto.class)
                .onErrorMap(WageClientExceptions::new)
                .block();
    }

    public WageCreateResponseDto submitWageRequest(String requestId) {
        return webClient
                .patch()
                .uri(wageSubmitUri.replace("{id}", requestId))
                .header(HttpHeaders.AUTHORIZATION, "tokenClient.getUserToken()")
                .retrieve()
                .bodyToMono(WageCreateResponseDto.class)
                .onErrorMap(WageClientExceptions::new)
                .block();
    }

    public WageCreateResponseDto cancelWageRequest(String requestId) {
        return webClient
                .delete()
                .uri(wageCancelUri.replace("{id}", requestId))
                .header(HttpHeaders.AUTHORIZATION, "tokenClient.getUserToken()")
                .retrieve()
                .bodyToMono(WageCreateResponseDto.class)
                .onErrorMap(WageClientExceptions::new)
                .block();
    }
}*/
