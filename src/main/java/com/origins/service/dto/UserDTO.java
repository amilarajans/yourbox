/*
 * Copyright (c) 2016-2016, Origins Software and Web Solutions. All Rights Reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.origins.service.dto;

import com.origins.domain.Authority;
import com.origins.domain.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserDTO {

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private boolean activated = false;

    private String langKey;

    private Set<String> authorities;

    public UserDTO() {
    }

    public UserDTO(User user) {
        this(user.getLogin(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getActivated(), user.getLangKey(),
                user.getAuthorities().stream().map(Authority::getName)
                        .collect(Collectors.toSet()));
    }

    public UserDTO(String login, String firstName, String lastName,
                   String email, boolean activated, String langKey, Set<String> authorities) {

        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.activated = activated;
        this.langKey = langKey;
        this.authorities = authorities;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActivated() {
        return activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return String.format("UserDTO{login='%s', firstName='%s', lastName='%s', email='%s', activated=%s, langKey='%s', authorities=%s}", login, firstName, lastName, email, activated, langKey, authorities);
    }
}
