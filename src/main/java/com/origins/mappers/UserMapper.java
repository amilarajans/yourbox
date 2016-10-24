/*
 * Copyright (c) 2016-2016, Origins Software and Web Solutions. All Rights Reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.origins.mappers;

import com.origins.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by Amila-Kumara on 18/10/2016.
 */
@Mapper
public interface UserMapper {
    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(ZonedDateTime dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    Optional<User> findOneById(Long userId);

    //    @Query(value = "select distinct user from User user left join fetch user.authorities",
//            countQuery = "select count(user) from User user")
    List<User> findAllWithAuthorities();

    void delete(User user);

    void save(User user);

    User findOne(Long id);
}
