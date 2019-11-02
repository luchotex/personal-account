package com.g2.personalaccount.controllers;

import com.g2.personalaccount.services.AccountConfirmationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 14:19
 */
@Controller
public class ConfirmationController {

  private AccountConfirmationService accountConfirmationService;

  public ConfirmationController(AccountConfirmationService accountConfirmationService) {
    this.accountConfirmationService = accountConfirmationService;
  }

  @RequestMapping(path = "/creation-confirmation/{confirmationId}")
  public String index(@PathVariable("confirmationId") String confirmationId) {
    return accountConfirmationService.confirmCreation(confirmationId);
  }
}
