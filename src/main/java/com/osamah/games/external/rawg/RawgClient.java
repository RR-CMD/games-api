package com.osamah.games.external.rawg;

import com.osamah.games.external.rawg.dto.RawgGameResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rawgClient", url = "${rawg.api.url}", configuration = RawgConfig.class)
public interface RawgClient {

    @GetMapping("/games/{slug}")
    RawgGameResponse getGameDetails(@PathVariable("slug") String slug);

}