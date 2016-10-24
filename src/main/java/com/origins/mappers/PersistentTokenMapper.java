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

import com.origins.domain.PersistentToken;
import com.origins.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Amila-Kumara on 18/10/2016.
 */
@Mapper
public interface PersistentTokenMapper {
    List<PersistentToken> findByUser(User user);

    List<PersistentToken> findByTokenDateBefore(LocalDate localDate);

    void delete(PersistentToken token);

    void delete(String decodedSeries);

    void saveAndFlush(PersistentToken token);

    PersistentToken findOne(String presentedSeries);
}
