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

import com.origins.domain.Authority;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Amila-Kumara on 18/10/2016.
 */
@Mapper
public interface AuthorityMapper {
    Authority findOne(String role);
}