/*
 * Copyright (c) 2016-2016, Origins Software and Web Solutions. All Rights Reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.origins.web;

import com.codahale.metrics.annotation.Timed;
import com.origins.domain.PersistentToken;
import com.origins.domain.User;
import com.origins.mappers.PersistentTokenMapper;
import com.origins.mappers.UserMapper;
import com.origins.security.SecurityUtils;
import com.origins.service.MailService;
import com.origins.service.UserService;
import com.origins.service.dto.UserDTO;
import com.origins.utils.HeaderUtil;
import com.origins.web.dao.KeyAndPasswordDao;
import com.origins.web.dao.ManagedUserDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Inject
    private UserService userService;

    @Autowired
    private UserMapper userRepository;

    @Autowired
    private PersistentTokenMapper persistentTokenRepository;

    @Inject
    private MailService mailService;

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @param request       the HTTP request
     * @return the ResponseEntity with status 201 (Created) if the user is registered or 400 (Bad Request) if the login or e-mail is already in use
     */
    @PostMapping(value = "/register", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Timed
    public ResponseEntity<?> registerAccount(@RequestBody ManagedUserDao managedUserVM, HttpServletRequest request) {

        HttpHeaders textPlainHeaders = new HttpHeaders();
        textPlainHeaders.setContentType(MediaType.TEXT_PLAIN);

        return userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase())
                .map(user -> new ResponseEntity<>("login already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
                .orElseGet(() -> userRepository.findOneByEmail(managedUserVM.getEmail())
                        .map(user -> new ResponseEntity<>("e-mail address already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
                        .orElseGet(() -> {
                            User user = userService.createUser(managedUserVM.getLogin(), managedUserVM.getPassword(),
                                    managedUserVM.getFirstName(), managedUserVM.getLastName(), managedUserVM.getEmail().toLowerCase(),
                                    managedUserVM.getLangKey());
                            String baseUrl = request.getScheme() + // "http"
                                    "://" +                                // "://"
                                    request.getServerName() +              // "myhost"
                                    ":" +                                  // ":"
                                    request.getServerPort() +              // "80"
                                    request.getContextPath();              // "/myContextPath" or "" if deployed in root context

                            mailService.sendActivationEmail(user, baseUrl);
                            return new ResponseEntity<>(HttpStatus.CREATED);
                        })
                );
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping(value = "/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key)
                .map(user -> new ResponseEntity<String>(HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserDTO> getAccount() {
        return Optional.ofNullable(userService.getUserWithAuthorities())
                .map(user -> new ResponseEntity<>(new UserDTO(user), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal Server Error) if the user couldn't be updated
     */
    @PostMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> saveAccount(@RequestBody UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findOneByEmail(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userDTO.getLogin()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use")).body(null);
        }
        return userRepository
                .findOneByLogin(SecurityUtils.getCurrentUserLogin())
                .map(u -> {
                    userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getLangKey());
                    return new ResponseEntity<String>(HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account/change_password : changes the current user's password
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new password is not strong enough
     */
    @PostMapping(value = "/account/change_password", produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> changePassword(@RequestBody String password) {
        if (!checkPasswordLength(password)) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * GET  /account/sessions : get the current open sessions.
     *
     * @return the ResponseEntity with status 200 (OK) and the current open sessions in body,
     * or status 500 (Internal Server Error) if the current open sessions couldn't be retrieved
     */
    @GetMapping(value = "/account/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<PersistentToken>> getCurrentSessions() {
        return userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin())
                .map(user -> new ResponseEntity<>(
                        persistentTokenRepository.findByUser(user.getId()),
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * DELETE  /account/sessions?series={series} : invalidate an existing session.
     * <p>
     * - You can only delete your own sessions, not any other user's session
     * - If you delete one of your existing sessions, and that you are currently logged in on that session, you will
     * still be able to use that session, until you quit your browser: it does not work in real time (there is
     * no API for that), it only removes the "remember me" cookie
     * - This is also true if you invalidate your current session: you will still be able to use it until you close
     * your browser or that the session times out. But automatic login (the "remember me" cookie) will not work
     * anymore.
     * There is an API to invalidate the current session, but there is no API to check which session uses which
     * cookie.
     *
     * @param series the series of an existing session
     * @throws UnsupportedEncodingException if the series couldnt be URL decoded
     */
    @DeleteMapping(value = "/account/sessions/{series}")
    @Timed
    public void invalidateSession(@PathVariable String series) throws UnsupportedEncodingException {
        String decodedSeries = URLDecoder.decode(series, "UTF-8");
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(u -> persistentTokenRepository.findByUser(u.getId()).stream()
                .filter(persistentToken -> StringUtils.equals(persistentToken.getSeries(), decodedSeries))
                .findAny().ifPresent(t -> persistentTokenRepository.deleteBySeries(decodedSeries)));
    }

    /**
     * POST   /account/reset_password/init : Send an e-mail to reset the password of the user
     *
     * @param mail    the mail of the user
     * @param request the HTTP request
     * @return the ResponseEntity with status 200 (OK) if the e-mail was sent, or status 400 (Bad Request) if the e-mail address is not registered
     */
    @PostMapping(value = "/account/reset_password/init", produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> requestPasswordReset(@RequestBody String mail, HttpServletRequest request) {
        return userService.requestPasswordReset(mail)
                .map(user -> {
                    String baseUrl = request.getScheme() +
                            "://" +
                            request.getServerName() +
                            ":" +
                            request.getServerPort() +
                            request.getContextPath();
                    mailService.sendPasswordResetMail(user, baseUrl);
                    return new ResponseEntity<>("e-mail was sent", HttpStatus.OK);
                }).orElse(new ResponseEntity<>("e-mail address not registered", HttpStatus.BAD_REQUEST));
    }

    /**
     * POST   /account/reset_password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset,
     * or status 400 (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(value = "/account/reset_password/finish", produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<String> finishPasswordReset(@RequestBody KeyAndPasswordDao keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        return userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
                .map(user -> new ResponseEntity<String>(HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private boolean checkPasswordLength(String password) {
        return (!StringUtils.isEmpty(password) &&
                password.length() >= ManagedUserDao.PASSWORD_MIN_LENGTH &&
                password.length() <= ManagedUserDao.PASSWORD_MAX_LENGTH);
    }
}
