package com.g2.personalaccount.dto.mappers;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.model.Account;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:38
 */
@Mapper
@DecoratedWith(AccountMapperDecorator.class)
public interface AccountMapper {

  @Mapping(target = "accountHolder.firstName", source = "holderFirstName")
  @Mapping(target = "accountHolder.lastName", source = "holderLastName")
  @Mapping(target = "accountHolder.accountHolderId.ssn", source = "ssn")
  @Mapping(target = "accountHolder.accountHolderId.voterCardId", source = "voterCardId")
  Account toEntity(AccountRequest request);

  @Mapping(target = "accountNumber", source = "id")
  @Mapping(target = "holderFirstName", source = "accountHolder.firstName")
  @Mapping(target = "holderLastName", source = "accountHolder.lastName")
  @Mapping(target = "ssn", source = "accountHolder.accountHolderId.ssn")
  @Mapping(target = "voterCardId", source = "accountHolder.accountHolderId.voterCardId")
  AccountResponse toResponse(Account account);

  @Mapping(target = "accountHolder.firstName", source = "holderFirstName")
  @Mapping(target = "accountHolder.lastName", source = "holderLastName")
  @Mapping(target = "accountHolder.accountHolderId.ssn", source = "ssn")
  @Mapping(target = "accountHolder.accountHolderId.voterCardId", source = "voterCardId")
  void toEntity(AccountUpdateRequest request, @MappingTarget Account account);
}