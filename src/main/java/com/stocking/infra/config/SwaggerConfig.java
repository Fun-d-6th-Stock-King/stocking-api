package com.stocking.infra.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import com.stocking.infra.common.FirebaseUser;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Swagger 2 Configuration
 */
@Configuration
@Profile({ "local", "develop", "production" })
@EnableSwagger2WebMvc
public class SwaggerConfig {

    public static final String BR = "\n\r"; // @ApiOperation notes Line 구분자
    public static final String REQUIRED = "<b>&nbsp;<em>required</em></b>"; // @ApiOperation notes required

    /**
     * [finance] group swagger Docket
     * 
     * @return <Docket>
     */
    @Bean
    public Docket financeApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("finance").select()
                .apis(RequestHandlerSelectors.basePackage("com.stocking.modules.stock")).paths(PathSelectors.any())
                .build();

        return setDocketCommonConfig(docket, "[finance] API", "주식시장 API");
    }

    @Bean
    public Docket accountApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("account").select()
                .apis(RequestHandlerSelectors.basePackage("com.stocking.modules.account")).paths(PathSelectors.any())
                .build();

        return setDocketCommonConfig(docket, "[account] API", "유저 API");
    }
    
    @Bean
    public Docket buyOrNotApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(FirebaseUser.class)
                .groupName("buyornot").select()
                .apis(RequestHandlerSelectors.basePackage("com.stocking.modules.buyornot")).paths(PathSelectors.any())
                .build();

        return setDocketCommonConfig(docket, "[buyOrNot] API", "살까말까 API");
    }
    
    @Bean
    public Docket buyThenApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(FirebaseUser.class)
                .groupName("buythen").select()
                .apis(RequestHandlerSelectors.basePackage("com.stocking.modules.buythen")).paths(PathSelectors.any())
                .build();

        return setDocketCommonConfig(docket, "[buyThen] API", "그때살껄 API");
    }

    /**
     * Set Docket Common Config
     *
     * @param docket    <Docket>
     * @param group     <String>
     * @param groupName <String>
     * @return <Docket>
     */
    private Docket setDocketCommonConfig(Docket docket, String group, String groupName) {
        return docket.apiInfo(this.getApiInfo(group, groupName))
                /**
                 * useDefaultResponseMessages(true) TRUE 기본에러코드 사용 FALSE 모든 메서드 기본에러코드 초기화(세팅한 값
                 * 외에도 초기화 됨)
                 */
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, this.responseMessageSetGet())
                .globalResponseMessage(RequestMethod.POST, this.responseMessageSetPost())
                .globalResponseMessage(RequestMethod.DELETE, this.responseMessageSetDelete())
                .globalResponseMessage(RequestMethod.PUT, this.responseMessageSetPut())
    			.securityContexts(Arrays.asList(securityContext()))
    			.securitySchemes(Arrays.asList(apiKey()));
    }

    /**
     * API Information
     * 
     * @param group     <String>
     * @param groupName <String>
     * @return <ApiInfo>
     */
    private ApiInfo getApiInfo(String title, String description) {
        return new ApiInfoBuilder().title(title).description(description).version("1.0.0").build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private Area (Error Model)
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private ArrayList<ResponseMessage> responseMessageSetCommon() {
        ArrayList<ResponseMessage> responseMessageCommon = new ArrayList<>();
        // 400 : BAD_REQUEST
        responseMessageCommon.add(new ResponseMessageBuilder().code(HttpStatus.BAD_REQUEST.value())
                .message(HttpStatus.BAD_REQUEST.getReasonPhrase()).build());

        // 404 : NOT_FOUND
        responseMessageCommon.add(new ResponseMessageBuilder().code(HttpStatus.NOT_FOUND.value())
                .message(HttpStatus.NOT_FOUND.getReasonPhrase()).build());

        // 422 : UNPROCESSABLE_ENTITY
        responseMessageCommon.add(new ResponseMessageBuilder().code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()).build());

        // 500 : INTERNAL_SERVER_ERROR
        responseMessageCommon.add(new ResponseMessageBuilder().code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).build());

        return responseMessageCommon;
    }

    /**
     * GET Method Response Message Set
     * 
     * @return <ArrayList>
     */
    private ArrayList<ResponseMessage> responseMessageSetGet() {
        return this.responseMessageSetCommon();
    }

    /**
     * POST Method Response Message Set
     * 
     * @return <ArrayList>
     */
    private ArrayList<ResponseMessage> responseMessageSetPost() {
        ArrayList<ResponseMessage> responseMessageSetPost = this.responseMessageSetCommon();

        // 409 : CONFLICT
        responseMessageSetPost.add(new ResponseMessageBuilder().code(HttpStatus.CONFLICT.value())
                .message(HttpStatus.CONFLICT.getReasonPhrase()).build());

        return responseMessageSetPost;
    }

    /**
     * PUT Method Response Message Set
     * 
     * @return <ArrayList>
     */
    private ArrayList<ResponseMessage> responseMessageSetPut() {
        return this.responseMessageSetCommon();
    }

    /**
     * DELETE Method Response Message Set
     * 
     * @return <ArrayList>
     */
    private ArrayList<ResponseMessage> responseMessageSetDelete() {
        return this.responseMessageSetCommon();
    }

	private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }
	
	private SecurityContext securityContext() {
        return springfox
                .documentation
                .spi.service
                .contexts
                .SecurityContext
                .builder()
                .securityReferences(defaultAuth()).forPaths(PathSelectors.any()).build();
    }

	List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
    }
}