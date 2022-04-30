package epsi.mspr.mingf.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class OtpValideRequest {
    @NotBlank
    private String token;

    @NotNull
    private int codeOtp;

    public OtpValideRequest(){}

}
