/*
 * Copyright (c) 2016-2016, Origins Software and Web Solutions. All Rights Reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.origins.utils;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;

/**
 * Utility class for handling pagination.
 * <p>
 * <p>
 * Pagination uses the same principles as the <a href="https://developer.github.com/v3/#pagination">Github API</a>,
 * and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 */
public final class PaginationUtil {

    private PaginationUtil() {
    }

//    public static HttpHeaders generatePaginationHttpHeaders(Page<?> page, String baseUrl)
//        throws URISyntaxException {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("X-Total-Count", "" + page.getTotalElements());
//        String link = "";
//        if ((page.getNumber() + 1) < page.getTotalPages()) {
//            link = "<" + generateUri(baseUrl, page.getNumber() + 1, page.getSize()) + ">; rel=\"next\",";
//        }
//        // prev link
//        if ((page.getNumber()) > 0) {
//            link += "<" + generateUri(baseUrl, page.getNumber() - 1, page.getSize()) + ">; rel=\"prev\",";
//        }
//        // last and first link
//        int lastPage = 0;
//        if (page.getTotalPages() > 0) {
//            lastPage = page.getTotalPages() - 1;
//        }
//        link += "<" + generateUri(baseUrl, lastPage, page.getSize()) + ">; rel=\"last\",";
//        link += "<" + generateUri(baseUrl, 0, page.getSize()) + ">; rel=\"first\"";
//        headers.add(HttpHeaders.LINK, link);
//        return headers;
//    }

    private static String generateUri(String baseUrl, int page, int size) throws URISyntaxException {
        return UriComponentsBuilder.fromUriString(baseUrl).queryParam("page", page).queryParam("size", size).toUriString();
    }
}
