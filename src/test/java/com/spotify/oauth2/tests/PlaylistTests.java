package com.spotify.oauth2.tests;

import com.spotify.oauth2.api.StatusCode;
import com.spotify.oauth2.api.applicationApi.PlaylistApi;
import com.spotify.oauth2.pojo.Playlist;
import com.spotify.oauth2.utils.ConfigLoader;
import com.spotify.oauth2.utils.DataLoader;
import io.qameta.allure.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.spotify.oauth2.api.SpecBuilder.getRequestSpec;
import static com.spotify.oauth2.api.SpecBuilder.getResponseSpec;
import static com.spotify.oauth2.utils.FakerUtils.generateDescription;
import static com.spotify.oauth2.utils.FakerUtils.generateName;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import io.restassured.response.Response;

public class PlaylistTests extends BaseTest {

    @Owner("Sachin Khandelwal")
    @Link("https://example.org")
    @Link(name = "allure", type = "mylink")
    @TmsLink("TMS-456")
    @Issue("AUTH-123")
    @Description("This is a description")
    @Test(description = "should be able to create a playlist")
    public void ShouldBeAbleToCreateAPlaylist(){
      //  Playlist requestPlaylist = playlistBuilder(generateName(), generateDescription(), false);
        Playlist requestPlaylist = playlistBuilder("New Playlist", "New playlist description", false);
        Response response = PlaylistApi.post(requestPlaylist);
        assertStatusCode(response.getStatusCode(), StatusCode.CODE_201.code);
        assertPlaylistEqual(response.as(Playlist.class), requestPlaylist );
        // Playlist responsePlaylist = response.as(Playlist.class);
    }

    @Test
    public void ShouldBeAbleToGetAPlaylist(){
        Playlist requestPlaylist = playlistBuilder("Updated Playlist", "Updated playlist description", true);
        Response response = PlaylistApi.get(DataLoader.getInstance().getGetPlaylistId());
        assertStatusCode(response.getStatusCode(), StatusCode.CODE_200.code);
        assertPlaylistEqual(response.as(Playlist.class), requestPlaylist );
    }

    @Test
    public void ShouldBeAbleToUpdateAPlaylist(){
        Playlist requestPlaylist = playlistBuilder("Updated Playlist", "Updated playlist description", false);
        Response response = PlaylistApi.update(DataLoader.getInstance().getUpdatePlaylistId(), requestPlaylist);
        assertStatusCode(response.getStatusCode(), StatusCode.CODE_200.code);
    }

    @Step
    public Playlist playlistBuilder(String name, String description, boolean _pubic){
        return Playlist.builder().
        name(name).
        description(description).
        _public(_pubic).build();
    }

    @Step
    public void assertPlaylistEqual(Playlist responsePlaylist, Playlist requestPlaylist){
        assertThat(responsePlaylist.getName(), equalTo(requestPlaylist.getName()));
        assertThat(responsePlaylist.getDescription(), equalTo(requestPlaylist.getDescription()));
        assertThat(responsePlaylist.get_public(), equalTo(requestPlaylist.get_public()));
    }

    @Step
    public void assertStatusCode(int actualStatusCode, int expectedStatusCode){
        assertThat(actualStatusCode, equalTo(expectedStatusCode));
    }
}
