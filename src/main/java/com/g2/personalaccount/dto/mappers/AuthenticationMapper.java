package com.g2.personalaccount.dto.mappers;

import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.requests.ExternalMoneyMovementRequest;
import org.mapstruct.Mapper;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 14:53
 */
@Mapper
public interface AuthenticationMapper {

  AuthenticationRequest toAuthenticationRequest(ExternalMoneyMovementRequest request);
}
