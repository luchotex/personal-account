package com.g2.personalaccount.controllers;

import com.g2.personalaccount.dto.responses.MoneyMovementResponse;
import com.g2.personalaccount.services.AccountConfirmationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 14:19
 */
@Api(value = "/confirmation")
@Controller
public class ConfirmationController {

  private AccountConfirmationService accountConfirmationService;

  public ConfirmationController(AccountConfirmationService accountConfirmationService) {
    this.accountConfirmationService = accountConfirmationService;
  }

  @RequestMapping(path = "/creation-confirmation/{confirmationId}")
  @ApiOperation(
      value =
          "This call is performed to confirm the account creation, this redirects to different pages:\n"
              + "If is correctly confirmed, "
              + "If was already confirmed, "
              + "If the confirmation doesn't exists, "
              + "If the confirmation expires; this date is defined on configurations.\n",
      response = MoneyMovementResponse.class)
  public String index(@PathVariable("confirmationId") String confirmationId) {
    return accountConfirmationService.confirmCreation(confirmationId);
  }
}
