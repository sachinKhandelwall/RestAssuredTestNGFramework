package com.spotify.oauth2.api.applicationApi;

import com.spotify.oauth2.api.RestUtils;
import com.spotify.oauth2.pojo.Playlist;
import com.spotify.oauth2.utils.ConfigLoader;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static com.spotify.oauth2.api.Route.PLAYLISTS;
import static com.spotify.oauth2.api.Route.USERS;
import static com.spotify.oauth2.api.TokenManager.getToken;

public class PlaylistApi {
    @Step
    public static Response post(Playlist requestPlaylistPayload){
        return RestUtils.post(USERS + "/" + ConfigLoader.getInstance().getUser() + PLAYLISTS, getToken(), requestPlaylistPayload );
    }

    public static Response get(String playlistId){
        return RestUtils.get(PLAYLISTS + "/" + playlistId, getToken());
    }

    public static Response update(String playlistId, Playlist requestPlaylistPayload){
        return RestUtils.update(PLAYLISTS + "/" + playlistId, getToken(), requestPlaylistPayload );
    }
}
