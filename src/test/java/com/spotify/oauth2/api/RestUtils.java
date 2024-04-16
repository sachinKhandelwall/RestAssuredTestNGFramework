package com.spotify.oauth2.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;

import static com.spotify.oauth2.api.Route.API;
import static com.spotify.oauth2.api.Route.TOKEN;
import static com.spotify.oauth2.api.SpecBuilder.*;
import static io.restassured.RestAssured.given;

public class RestUtils {

    public static Response post(String path, String token, Object requestPayload){
        return given(getRequestSpec()).
                body(requestPayload).
                header("Authorization","Bearer " + token).
               // auth().oauth2(token). // we can also use oauth2 method of auth to send the Authorization token as well
                when().post(path).
                then().spec(getResponseSpec()).
                extract().
                response();
    }

    public static Response postAccount(HashMap<String, String> formParams){
        return given(getAccountRequestSpec()).
                formParams(formParams).
                when().post(API + TOKEN).
                then().spec(getResponseSpec()).
                extract().
                response();
    }

    public static Response get(String path, String token){
        return given(getRequestSpec()).
                header("Authorization","Bearer " + token).
                // auth().oauth2(token). // we can also use oauth2 method of auth to send the Authorization token as well
                when().get(path).
                then().spec(getResponseSpec()).
                extract().
                response();
    }

    public static Response update(String path, String token, Object requestPayload){
        return given(getRequestSpec()).
                header("Authorization","Bearer " + token).
                // auth().oauth2(token). // we can also use oauth2 method of auth to send the Authorization token as well
                body(requestPayload).
                when().put(path).
                then().spec(getResponseSpec()).
                extract().
                response();

    }
}
