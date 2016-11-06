/*
 * Copyright (c) 2016-2016, Origins Software and Web Solutions. All Rights Reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.origins.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Persistent tokens are used by Spring Security to automatically log in users.
 *
 * @see com.origins.security.CustomPersistentRememberMeServices
 */
public class PersistentToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_USER_AGENT_LEN = 255;

    private String series;

    @JsonIgnore
    private String tokenValue;

    private LocalDate tokenDate;

    //an IPV6 address max length is 39 characters
    private String ipAddress;

    private String userAgent;

    @JsonIgnore
    private User user;

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public LocalDate getTokenDate() {
        return tokenDate;
    }

    public void setTokenDate(LocalDate tokenDate) {
        this.tokenDate = tokenDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        if (userAgent.length() >= MAX_USER_AGENT_LEN) {
            this.userAgent = userAgent.substring(0, MAX_USER_AGENT_LEN - 1);
        } else {
            this.userAgent = userAgent;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersistentToken that = (PersistentToken) o;

        if (!series.equals(that.series)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return series.hashCode();
    }

    @Override
    public String toString() {
        return String.format("PersistentToken{series='%s', tokenValue='%s', tokenDate=%s, ipAddress='%s', userAgent='%s'}", series, tokenValue, tokenDate, ipAddress, userAgent);
    }
}
