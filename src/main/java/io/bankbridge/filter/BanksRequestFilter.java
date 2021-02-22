package io.bankbridge.filter;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankRequestFilterModel;
import io.bankbridge.seedwork.Constants;

import spark.Request;

/** Class to do manipulation on records with pagination and filter */
public class BanksRequestFilter {

        /**
         * Method to retrieve the filters from the Spark request. The filter parameters
         * are passed as query strings.
         * 
         * @param request
         * @return The BankRequestFilterModel with pageNumber, pageSize, id, name,
         *         countrycode, product and auth. For unpassed filter parameter, default
         *         value or empty string is set to the model.
         */
        public BankRequestFilterModel getRequestFilters(Request request) {

                Integer pageNumber = !Strings.isNullOrEmpty(request.queryParams("pagenumber"))
                                ? Integer.parseInt(request.queryParams("pagenumber"))
                                : 1;

                Integer pageSize = !Strings.isNullOrEmpty(request.queryParams("pagesize"))
                                ? Integer.parseInt(request.queryParams("pagesize"))
                                : Constants.DEFAULT_PAGE_SIZE;

                String id = !Strings.isNullOrEmpty(request.queryParams("id")) ? request.queryParams("id").trim()
                                : Constants.STRING_EMPTY;

                String name = !Strings.isNullOrEmpty(request.queryParams("name")) ? request.queryParams("name").trim()
                                : Constants.STRING_EMPTY;

                String countryCode = !Strings.isNullOrEmpty(request.queryParams("countrycode"))
                                ? request.queryParams("countrycode").trim()
                                : Constants.STRING_EMPTY;

                String auth = !Strings.isNullOrEmpty(request.queryParams("auth")) ? request.queryParams("auth").trim()
                                : Constants.STRING_EMPTY;

                String product = !Strings.isNullOrEmpty(request.queryParams("product"))
                                ? request.queryParams("product").trim()
                                : Constants.STRING_EMPTY;

                return new BankRequestFilterModel(pageNumber, pageSize, id, name, countryCode, product, auth);
        }

        /**
         * Method to retrieve the filtered banks with specified page size and page
         * number
         * 
         * @param banksList
         * @param requestParam
         * @return List of filtered banks with specified number of records on specified
         *         page. For any invalid filter parameter, no filters will be applied
         *         and default record set will be returned.
         */
        public List<BankModel> getFilterBankModels(List<BankModel> banksList, BankRequestFilterModel requestParam) {

                /** Filter with Id */
                if (!Strings.isNullOrEmpty(requestParam.getId())) {
                        banksList = banksList.stream().filter(s -> s.getBic().equalsIgnoreCase(requestParam.getId()))
                                        .collect(Collectors.toList());
                }

                /** Filter with countrycode */
                if (!Strings.isNullOrEmpty(requestParam.getCountryCode())) {
                        banksList = banksList.stream()
                                        .filter(s -> s.getCountryCode().equalsIgnoreCase(requestParam.getCountryCode()))
                                        .collect(Collectors.toList());
                }

                /**
                 * Filter with product. For v1- Products will be there, where as for v2-products
                 * will be null for any invalid filter value, default results will be returned
                 * without filter
                 */
                if (!Strings.isNullOrEmpty(requestParam.getProduct())
                                && banksList.stream().anyMatch(p -> p.getProducts() != null)) {                                        
                        banksList = banksList.stream()
                        .filter(f -> f.getProducts().stream().anyMatch(c -> c.equalsIgnoreCase(requestParam.getProduct())))
                        .collect(Collectors.toList());                                      
                }

                /** Filter with name */
                if (!Strings.isNullOrEmpty(requestParam.getName())) {
                        banksList = banksList.stream().filter(
                                        s -> s.getName().toLowerCase().contains(requestParam.getName().toLowerCase()))
                                        .collect(Collectors.toList());
                }

                /** Filter with auth */
                if (!Strings.isNullOrEmpty(requestParam.getAuth())) {
                        banksList = banksList.stream().filter(s -> s.getAuth().equalsIgnoreCase(requestParam.getAuth()))
                                        .collect(Collectors.toList());
                }

                /** Filter with page number and page size */ 
                banksList = banksList.stream().skip(requestParam.getPageSize() * (requestParam.getPageNumber() - 1))
                                .limit(requestParam.getPageSize()).collect(Collectors.toList());

                return banksList;
        }
}
