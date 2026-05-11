package com.osamah.games.external.rawg.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class RawgGameResponseTest {

    @Autowired
    private JacksonTester<RawgGameResponse> json;

    @Test
    void rawgGameResponse_ShouldDeserialize_WhenEveryFieldPresent() throws Exception {
        String content = """
                        {
                            "slug": "witcher-3",
                            "name": "The Witcher 3",
                            "description_raw": "Game Description Test",
                            "released": "2015-05-19",
                            "background_image": "img_url",
                            "metacritic": 92,
                            "genres": [
                                { "name": "Action" },
                                { "name": "RPG" }
                            ],
                            "platforms": [
                                {
                                    "platform": {
                                        "id": 187,
                                        "name": "PlayStation 5",
                                        "slug": "playstation5",
                                        "image": null,
                                        "year_end": null,
                                        "year_start": 2020,
                                        "games_count": 1475,
                                        "image_background": "https://media.rawg.io/media/games/3ea/3ea3c9bbd940b6cb7f2139e42d3d443f.jpg"
                                    },
                                    "released_at": "2015-05-18",
                                    "requirements": {}
                                },
                                {
                                    "platform": {
                                        "id": 186,
                                        "name": "Xbox Series S/X",
                                        "slug": "xbox-series-x",
                                        "image": null,
                                        "year_end": null,
                                        "year_start": 2020,
                                        "games_count": 1247,
                                        "image_background": "https://media.rawg.io/media/games/26d/26d4437715bee60138dab4a7c8c59c92.jpg"
                                    },
                                    "released_at": "2015-05-18",
                                    "requirements": {}
                                }
                            ]
                        }
                """;

        RawgGameResponse response = json.parseObject(content);


        assertThat(response.slug()).isEqualTo("witcher-3");
        assertThat(response.title()).isEqualTo("The Witcher 3");
        assertThat(response.description()).isEqualTo("Game Description Test");
        assertThat(response.released()).isEqualTo(LocalDate.of(2015, 5, 19));
        assertThat(response.imageUrl()).isEqualTo("img_url");
        assertThat(response.metacritic()).isEqualTo(92);

        assertThat(response.genres()).hasSize(2);
        assertThat(response.genres()
                .getFirst()
                .name()).isEqualTo("Action");
        assertThat(response.genres()
                .getLast()
                .name()).isEqualTo("RPG");

        assertThat(response.platforms()).hasSize(2);
        assertThat(response.platforms()
                .getFirst()
                .platform()
                .name()).isEqualTo("PlayStation 5");
        assertThat(response.platforms()
                .getLast()
                .platform()
                .name()).isEqualTo("Xbox Series S/X");
    }

    @Test
    void rawgGameResponse_ShouldDeserialize_WhenFieldsMissing() throws Exception {

        String content = """
                {
                    "slug": "old-game",
                    "name": "Old Game 1982",
                    "description_raw": "Very Old Game",
                    "released": null,
                    "background_image": null,
                    "genres": []
                }
                """;

        RawgGameResponse response = json.parseObject(content);

        assertThat(response.slug()).isEqualTo("old-game");
        assertThat(response.title()).isEqualTo("Old Game 1982");

        assertThat(response.metacritic()).isNull();
        assertThat(response.released()).isNull();
        assertThat(response.imageUrl()).isNull();

        assertThat(response.genres()).isEqualTo(List.of());
    }
}