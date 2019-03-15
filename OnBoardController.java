package com.cde.service.onboarding.controller;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.cde.service.onboarding.excption.UserNotFoundException;
import com.cde.service.onboarding.model.EmailRequest;
import com.cde.service.onboarding.model.ErrorObject;
import com.cde.service.onboarding.model.OnboardDetailsResponse;
import com.cde.service.onboarding.model.entity.AssociateInfo;
import com.cde.service.onboarding.model.entity.AssosiateProductDetail;
import com.cde.service.onboarding.model.entity.CDEProductDetails;
import com.cde.service.onboarding.model.entity.OnboardDetails;
import com.cde.service.onboarding.repository.OnBoardDetailsRepository;
import com.cde.service.onboarding.service.AssociateInfoService;
import com.cde.service.onboarding.service.CDEProductDetailService;
import com.cde.service.onboarding.service.Impl.OnBoardServiceCFImpl;
import com.cde.service.onboarding.service.Impl.OnBoardServiceImpl;
import com.cde.service.onboarding.util.OnBoardConstants;
import com.cde.service.onboarding.util.OnBoardStatus;

@Controller
public class OnBoardController {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private AssociateInfoService associateInfoService;
  private CDEProductDetailService cdeProductDetailService;
  private OnBoardServiceImpl onBoardService;
  private OnBoardServiceCFImpl onBoardServiceCF;

  @Value("${spring.datasource.url}")
  private String springUrl;

  @Value("${spring.datasource.username}")
  private String springUsername;

  @Value("${spring.datasource.password}")
  private String springPassword;

  @Autowired
  OnBoardDetailsRepository repo;

  /**
   * 
   * @param cdeProductDetailService
   * @param associateInfoService
   * @param onBoardService
   */
  @Autowired
  public OnBoardController(CDEProductDetailService cdeProductDetailService, AssociateInfoService associateInfoService,
      OnBoardServiceImpl onBoardService, OnBoardServiceCFImpl onBoardServiceCF) {
    this.associateInfoService = associateInfoService;
    this.onBoardService = onBoardService;
    this.cdeProductDetailService = cdeProductDetailService;
    this.onBoardServiceCF = onBoardServiceCF;

  }

  /**
   * Redirect to register page
   * 
   * @return
   */
  // @RequestMapping(value = "/", method = RequestMethod.GET)
  // public String redirect() {
  // return "redirect:register";
  // }
  //
  // /**
  // * Return registration form template
  // *
  // * @param modelAndView
  // * @param associateInfo
  // * @return
  // */
  // @RequestMapping(value = "/register", method = RequestMethod.GET)
  // public ModelAndView showRegistrationPage(ModelAndView modelAndView,
  // AssociateInfo associateInfo) {
  //
  // modelAndView.addObject("associateInfo", associateInfo);
  // modelAndView.setViewName("index");
  // return modelAndView;
  // }

  /**
   * Process form input data
   * 
   * @param modelAndView
   * @param associateInfo
   * @param bindingResult
   * @param request
   * @return
   */
  /**
   * @RequestMapping(value = "/register", method = RequestMethod.POST) public
   *                       ModelAndView processRegistrationForm(ModelAndView
   *                       modelAndView,
   * @Valid @RequestBody AssociateInfo associateInfo, BindingResult bindingResult,
   *        HttpServletRequest request) {
   * 
   *        String baseUrl = String.format("%s://%s", request.getScheme(),
   *        request.getServerName()); LOG.info("associateInfo.getEmail()==>" +
   *        associateInfo.getEmail());
   *        LOG.info("associateInfo.getAssociateId()==>" +
   *        associateInfo.getAssociateId());
   *        LOG.info("associateInfo.getAssociateName()==>" +
   *        associateInfo.getFirstName());
   *        LOG.info("associateInfo.getAssociateDept()==>" +
   *        associateInfo.getEmail()); if (associateInfo.getEmail() != null &&
   *        !associateInfo.getEmail().contains("cognizant.com")) {
   *        modelAndView.addObject("notavalidinternalid", "Please provide only
   *        Cognizant User Email ID."); modelAndView.setViewName("register");
   *        bindingResult.reject("email"); }
   * 
   *        // Lookup user in database by e-mail // if (associateInfo.getEmail()
   *        != null) { AssociateInfo userExists =
   *        associateInfoService.findByEmailIgnoreCase(associateInfo.getEmail());
   * 
   *        LOG.info("Findby email completed"); if (userExists != null) {
   * 
   *        if
   *        (OnBoardStatus.NEW.toString().equalsIgnoreCase(userExists.getOnboardStatus()))
   *        {
   * 
   *        return showErrorPage(modelAndView, baseUrl,
   *        OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER, "Be Patient");
   * 
   *        } else if
   *        (OnBoardStatus.ACTIVE.toString().equalsIgnoreCase(userExists.getOnboardStatus())
   *        ||
   *        OnBoardStatus.REJECT.toString().equalsIgnoreCase(userExists.getOnboardStatus()))
   *        {
   * 
   *        return showErrorPage(modelAndView, baseUrl,
   *        OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER, "400");
   * 
   *        }
   * 
   *        }
   * 
   *        if (bindingResult.hasErrors()) {
   * 
   *        return showErrorPage(modelAndView, baseUrl,
   *        OnBoardConstants.ERROR_PRIMARY_TXT_DEFAULT, "500");
   * 
   *        } else {
   * 
   *        // new user so we create user and send confirmation e-mail // Generate
   *        encrypted email address that defines the string // token // for //
   *        confirmation link
   * 
   *        EmailRequest emailData = new EmailRequest();
   *        emailData.setBaseUrl(baseUrl); emailData.setSubject(
   *        MessageFormat.format(OnBoardConstants.EMAIL_SUBJECT,
   *        OnBoardConstants.APPROVE_REJECT_SUBJECT, associateInfo.getFirstName(),
   *        associateInfo.getAssociateDepartment()));
   * 
   *        associateInfo.setOnboardStatus(OnBoardStatus.NEW.getValue());
   * 
   *        associateInfoService.saveAssociateInfo(associateInfo);
   *        onBoardService.sendRegistrationEmail(emailData, associateInfo);
   * 
   *        modelAndView.addObject("confirmationMessage",
   *        OnBoardConstants.REGISTRATION_MAIL_NOTIFY);
   *        modelAndView.setViewName("register"); bindingResult.reject("submit");
   *        } // }
   * 
   *        return modelAndView; }
   */

  /**
   * Process form input data
   * 
   * // * @param modelAndView
   * 
   * @param associateInfo // * @param bindingResult
   * @param request
   * @return
   */
  @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> processRegistrationForm(@Valid @RequestBody AssociateInfo associateInfo, HttpServletRequest request) {

    String baseUrl = String.format("%s://%s", request.getScheme(), request.getServerName());
    LOG.info("associateInfo.getEmail()==>" + associateInfo.getEmail());
    LOG.info("associateInfo.getAssociateId()==>" + associateInfo.getAssociateId());
    LOG.info("associateInfo.getAssociateName()==>" + associateInfo.getFirstName());
    LOG.info("associateInfo.getAssociateDept()==>" + associateInfo.getAssociateDepartment());
    if (associateInfo.getEmail() != null && !associateInfo.getEmail().contains("cognizant.com")) {
      // modelAndView.addObject("notavalidinternalid", "Please provide
      // only Cognizant User Email ID.");
      // modelAndView.setViewName("register");
      // bindingResult.reject("email");
    }

    // Lookup user in database by e-mail
    // if (associateInfo.getEmail() != null) {
    AssociateInfo userExists = associateInfoService.findByEmailIgnoreCase(associateInfo.getEmail());

    LOG.info("Findby email completed");
    if (userExists != null) {
      LOG.info("userExists.getOnboardStatus()==>" + userExists.getOnboardStatus());
      if (OnBoardStatus.NEW.toString().equalsIgnoreCase(userExists.getOnboardStatus())) {
        // return new
        // ResponseEntity(OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER,
        // HttpStatus.OK);
        return showErrorPage("", OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER, HttpStatus.OK.toString());
        // OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER, "Be Patient");

      } else if (OnBoardStatus.ACTIVE.toString().equalsIgnoreCase(userExists.getOnboardStatus())
          || OnBoardStatus.REJECT.toString().equalsIgnoreCase(userExists.getOnboardStatus())) {
        // return new
        // ResponseEntity(OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER,
        // HttpStatus.BAD_REQUEST);
        return showErrorPage("", OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER, HttpStatus.BAD_REQUEST.toString());
        // OnBoardConstants.ERROR_PRIMARY_TXT_WAIT_USER, "400");

      }

    }

    // if (bindingResult.hasErrors()) {
    // return new ResponseEntity(OnBoardConstants.ERROR_PRIMARY_TXT_DEFAULT,
    // HttpStatus.INTERNAL_SERVER_ERROR);
    // return showErrorPage("", OnBoardConstants.ERROR_PRIMARY_TXT_DEFAULT,
    // HttpStatus.INTERNAL_SERVER_ERROR.toString());
    // OnBoardConstants.ERROR_PRIMARY_TXT_DEFAULT, "500");

    // } else {

    // new user so we create user and send confirmation e-mail
    // Generate encrypted email address that defines the string
    // token
    // for
    // confirmation link

    EmailRequest emailData = new EmailRequest();
    emailData.setBaseUrl(baseUrl);
    emailData.setSubject(MessageFormat.format(OnBoardConstants.EMAIL_SUBJECT, OnBoardConstants.APPROVE_REJECT_SUBJECT, associateInfo.getFirstName(),
        associateInfo.getAssociateDepartment()));

    associateInfo.setOnboardStatus(OnBoardStatus.NEW.getValue());

    associateInfoService.saveAssociateInfo(associateInfo);
    onBoardService.sendRegistrationEmail(emailData, associateInfo);

    // bindingResult.reject("submit");
    // }
    // }

    return new ResponseEntity(associateInfo, HttpStatus.OK);
  }

  /**
   * Process confirmation link
   * 
   * @param approveToken
   * @param rejectToken
   * @param request
   * @return
   */
  @RequestMapping(value = "/authorize", method = RequestMethod.GET)
  public String confirmRegistration(@RequestParam(name = "approve", required = false) String approveToken,
      @RequestParam(name = "reject", required = false) String rejectToken, HttpServletRequest request) {

    if ((StringUtils.isEmpty(approveToken) && StringUtils.isEmpty(rejectToken))
        || (!StringUtils.isEmpty(approveToken) && !StringUtils.isEmpty(rejectToken)))
      return "redirect:erruserstatus";

    boolean isApproveFlow = !StringUtils.isEmpty(approveToken);

    AssociateInfo info = isUserExist(isApproveFlow ? approveToken : rejectToken);

    if (info == null) {

      return "redirect:erruserstatus";
    }

    if (OnBoardStatus.ACTIVE.toString().equalsIgnoreCase(info.getOnboardStatus())
        || OnBoardStatus.REJECT.toString().equalsIgnoreCase(info.getOnboardStatus())) {

      return "redirect:erruserstatus";

    }
    // Check the user is Approved or Rejected
    if (isApproveFlow) {

      // Approve Flow
      request.setAttribute("associateinfo", info);
      return "forward:/approve";
    } else {

      // Reject Flow
      request.setAttribute("associateinfo", info);
      return "forward:/reject";

    }

  }

  private AssociateInfo isUserExist(String encryptedHash) {

    // Decrypt the hash
    String email = OnBoardConstants.decrypt(encryptedHash);

    // Lookup user in database by e-mail
    return associateInfoService.findByEmailIgnoreCase(email);
  }

  /**
   * User Status
   * 
   * @param modelAndView
   * @param bindingResult
   * @param request
   * @return
   */
  @RequestMapping(value = "/erruserstatus", method = RequestMethod.GET)
  public ModelAndView processErrorUserStatus(ModelAndView modelAndView, BindingResult bindingResult, HttpServletRequest request) {
    String baseUrl = String.format("%s://%s", request.getScheme(), request.getServerName());
    return showErrorPage(modelAndView, baseUrl, OnBoardConstants.ERROR_PRIMARY_TXT_APPROVE_REJECT_USER, "500");
  }

  /**
   * Approve user registration
   * 
   * @param modelAndView
   * @param bindingResult
   * @param request
   * @return
   */
  @RequestMapping(value = "/approve", method = RequestMethod.GET)
  public ModelAndView processApproveRequest(ModelAndView modelAndView, BindingResult bindingResult, HttpServletRequest request) {

    AssociateInfo info = (AssociateInfo) request.getAttribute("associateinfo");

    info.setOnboardStatus(OnBoardStatus.ACTIVE.getValue());

    EmailRequest emailData = new EmailRequest();
    emailData
        .setBaseUrl(onBoardService.getAWSUrlWithExpiry(OnBoardConstants.DOWNLOAD_LINK_BucketName, OnBoardConstants.DOWNLOAD_LINK_CLIENT_APP, 24));
    emailData.setSubject(MessageFormat.format(OnBoardConstants.EMAIL_SUBJECT, OnBoardConstants.APPROVE_EMAIL_SUBJECT, info.getFirstName(),
        info.getAssociateDepartment()));

    associateInfoService.saveAssociateInfo(info);

    onBoardService.sendApprovalEmail(emailData, info);

    modelAndView.addObject("name", info.getFirstName());
    modelAndView.addObject("email", info.getEmail());
    modelAndView.addObject("department", info.getAssociateDepartment());
    modelAndView.addObject("associateid", info.getAssociateId());

    modelAndView.setViewName("UserSuccessfullyOnboarded");

    return modelAndView;
  }

  /**
   * Reject User Registration
   * 
   * @param modelAndView
   * @param bindingResult
   * @param request
   * @return
   */
  @RequestMapping(value = "/reject", method = RequestMethod.GET)
  public ModelAndView processRejectRequest(ModelAndView modelAndView, BindingResult bindingResult, HttpServletRequest request) {

    AssociateInfo info = (AssociateInfo) request.getAttribute("associateinfo");
    modelAndView.addObject("name", info.getFirstName());
    modelAndView.addObject("email", info.getEmail());
    modelAndView.addObject("department", info.getAssociateDepartment());
    modelAndView.addObject("associateid", info.getAssociateId());

    modelAndView.setViewName("UserRejectOnboarded");

    info.setOnboardStatus(OnBoardStatus.REJECT.toString());
    associateInfoService.saveAssociateInfo(info);

    EmailRequest emailData = new EmailRequest();
    emailData.setSubject(
        MessageFormat.format(OnBoardConstants.EMAIL_SUBJECT, OnBoardConstants.REJECT_SUBJECT, info.getFirstName(), info.getAssociateDepartment()));

    onBoardService.sendRejectEmail(emailData, info);
    return modelAndView;
  }

  /**
   * Save user questionnerie
   * 
   * @param associateInfo
   * @return
   */
  @RequestMapping(value = "/save", method = RequestMethod.POST, produces = { "application/json" })
  public ResponseEntity<OnboardDetailsResponse> saveQuestionnerie(@Valid @RequestBody AssociateInfo associateInfo) {

    LOG.info(associateInfo.getEmail());
    AssociateInfo dbValue = associateInfoService.findByEmailIgnoreCase(associateInfo.getEmail());

    if (dbValue == null)
      throw new UserNotFoundException(MessageFormat.format(OnBoardConstants.EXCEPTION_USER_NOT_FOUND, associateInfo.getEmail()));

    /*
     * if (OnBoardStatus.ACTIVEWITHDATA.toString().equalsIgnoreCase(dbValue.
     * getOnboardStatus())) throw new UserNotFoundException(OnBoardConstants.
     * EXCEPTION_USER_ACTIVE_WITH_DATA);
     */
    // if not active or active with data throw error

    LOG.info("dbValue.getOnboardStatus() -> " + dbValue.getOnboardStatus());

    if (!(OnBoardStatus.ACTIVE.toString().equalsIgnoreCase(dbValue.getOnboardStatus())
        || OnBoardStatus.ACTIVEWITHDATA.toString().equalsIgnoreCase(dbValue.getOnboardStatus())))
      throw new UserNotFoundException(OnBoardConstants.EXCEPTION_USER_NOT_ACTIVE);

    // Set CDE Product chosen by the User

    for (OnboardDetails boardingDetail : associateInfo.getOnboarddetails()) {

      for (AssosiateProductDetail detail : boardingDetail.getAssosiateproductdetail()) {
        CDEProductDetails productDetail = cdeProductDetailService.findByProductName(detail.getCdeProductName());
        detail.setCdeProductID(productDetail.getId());
      }
      boardingDetail.setEmail(dbValue.getEmail());
    }

    LOG.info("" + associateInfo);

    dbValue.addChild(associateInfo.getOnboarddetails());

    LOG.info("**** " + dbValue);

    dbValue.setOnboardStatus(OnBoardStatus.ACTIVEWITHDATA.getValue());
    dbValue.setRegistrationStatus("INSTALLED");
    // Save the Update information
    associateInfoService.saveAssociateInfo(dbValue);

    // Note: Need to be changed based on the product selection. Code to be
    // updated
    OnboardDetailsResponse resp = new OnboardDetailsResponse();
    Map<String, String> responseUrls = new HashMap<>();
    // responseUrls.put("VSM", "registry-dev.apps.dev.cloudsprint.io/vsm");
    responseUrls.put("VSM", "linux=digicloud/vsm-docker-ai-linux,windows=digicloud/vsm-docker-windows,mac=digicloud/vsm-docker-ai-linux");

    resp.setResponseUrls(responseUrls);
    resp.setEmail(associateInfo.getEmail());

    LOG.info("Bfre -- " + dbValue);
    onBoardServiceCF.processPCFProcess(dbValue);

    return new ResponseEntity<OnboardDetailsResponse>(resp, HttpStatus.OK);
  }

  /**
   * Get User Detail
   * 
   * @param email
   * @return
   */
  @RequestMapping(value = "/userdetail/{email}", method = RequestMethod.GET, produces = { "application/json" })
  public ResponseEntity<AssociateInfo> getUserDetail(@Valid @Email @PathVariable("email") String email) {

    AssociateInfo userExists = associateInfoService.findByEmailIgnoreCase(email);

    LOG.info(email + "  " + (userExists == null));
    if (userExists == null)
      throw new UserNotFoundException(MessageFormat.format(OnBoardConstants.EXCEPTION_USER_NOT_FOUND, email));

    Set<OnboardDetails> resp = repo.findAll().stream().filter(deatail -> {
      return deatail.getEmail().equalsIgnoreCase(email);
    }).collect(Collectors.toSet());

    LOG.info("", resp.size());

    LOG.info("", repo.count());

    String onboard = "";

    for (OnboardDetails detail : resp) {
      onboard += detail;
    }

    onboard += "}";

    LOG.info(onboard);

    userExists.setOnboarddetails(resp);

    if (userExists.getRegistrationStatus() != null) {

      OnboardDetailsResponse onBoardDetailsResp = new OnboardDetailsResponse();
      Map<String, String> responseUrls = new HashMap<>();
      responseUrls.put("VSM", "linux=digicloud/vsm-docker-ai-linux,windows=digicloud/vsm-docker-windows,mac=digicloud/vsm-docker-ai-linux");

      onBoardDetailsResp.setResponseUrls(responseUrls);
      onBoardDetailsResp.setEmail(userExists.getEmail());
      userExists.setOnBoardDetailsResponse(onBoardDetailsResp);
    }

    LOG.info("Associate Info in user detail---->", userExists);

    return new ResponseEntity<AssociateInfo>(userExists, HttpStatus.OK);
  }

  /**
   * Update Registration status
   * 
   * @param email
   * @param status
   * @return
   */
  @RequestMapping(value = "/updateregistrationstatus/{email}/{status}", method = RequestMethod.GET, produces = { "application/json" })
  public ResponseEntity<Object> updateUserRegistrationDetail(@Valid @Email @PathVariable(value = "email") String email,
      @PathVariable(value = "status") String status) {

    AssociateInfo userExists = associateInfoService.findByEmailIgnoreCase(email);

    if (userExists == null)
      throw new UserNotFoundException(MessageFormat.format(OnBoardConstants.EXCEPTION_USER_NOT_FOUND, email));

    userExists.setRegistrationStatus(status);

    associateInfoService.saveAssociateInfo(userExists);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Set error
   * 
   * @param modelAndView
   * @param baseUrl
   * @param primarytext
   * @param error
   * @return
   */
  private ModelAndView showErrorPage(ModelAndView modelAndView, String baseUrl, String primarytext, String error) {

    modelAndView.addObject("errorCode", error);
    modelAndView.addObject("homepage", baseUrl);
    modelAndView.addObject("primarytext", primarytext);
    modelAndView.setViewName("error");

    return modelAndView;
  }

  private ResponseEntity<Object> showErrorPage(String baseUrl, String primaryTest, String errorCode) {

    ErrorObject errorObject = new ErrorObject();
    errorObject.setErrorCode(errorCode);
    errorObject.setHomepage(baseUrl);
    errorObject.setPrimarytext(primaryTest);

    LOG.info("ErrorObject -> " + errorObject.toString());

    return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    // return errorObject;

  }

  @GetMapping(value = "/viewtables")
  public ModelAndView showRegistrationPage(ModelAndView modelAndView) {
    List<String> tables = new ArrayList<>();
    try (Connection connection = DriverManager.getConnection(springUrl, springUsername, springPassword)) {
      DatabaseMetaData metadata = connection.getMetaData();
      String[] table = { "TABLE" };
      ResultSet rs = null;

      // receive the Type of the object in a String array.
      rs = metadata.getTables(null, null, null, table);
      while (rs.next()) {
        if (!rs.getString("TABLE_NAME").equalsIgnoreCase("sys_config"))
          tables.add(rs.getString("TABLE_NAME"));
      }
    } catch (SQLException e) {
      LOG.error("Error in getting tables", e);
    }
    modelAndView.addObject("tables", tables);
    modelAndView.setViewName("viewtables");
    return modelAndView;
  }

  @PostMapping(value = "/processCountry")
  public ModelAndView showTables(ModelAndView modelAndView, HttpServletRequest request) {
    String selectedtable = request.getParameter("tabledet");
    List<String> tableColumns = new ArrayList<>();
    List<List<String>> tableValueAll = new ArrayList<>();
    LOG.debug("Selected Table : {}", selectedtable);
    try (Connection connection = DriverManager.getConnection(springUrl, springUsername, springPassword)) {
      try (Statement statement = connection.createStatement()) {
        try (ResultSet results = statement.executeQuery("SELECT * FROM " + selectedtable)) {
          // Get resultset metadata

          ResultSetMetaData metadata = results.getMetaData();

          int columnCount = metadata.getColumnCount();
          // Get the column names; column indices start from 1

          for (int i = 1; i <= columnCount; i++) {
            String columnName = metadata.getColumnName(i);
            tableColumns.add(columnName);
            LOG.debug("Column Name : {}", columnName);
          }
          while (results.next()) {
            List<String> tableValues = new ArrayList<>();
            tableColumns.forEach(column -> {
              try {
                tableValues.add(results.getString(column));
              } catch (SQLException e) {
                LOG.error("Error in getting column values", e);
              }
            });
            tableValueAll.add(tableValues);
          }
        }
      }

    } catch (SQLException e) {
      LOG.error("Error in getting columns and column values", e);
    }
    modelAndView.addObject("tables", tableColumns);
    modelAndView.addObject("tableValue", tableValueAll);
    modelAndView.setViewName("processCountry");
    return modelAndView;
  }

}
