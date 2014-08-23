/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.mobilecloud.video;

import com.google.common.collect.Lists;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class VideoController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */

    public static final String TITLE_PARAMETER = "title";

    public static final String DURATION_PARAMETER = "duration";

    // The path where we expect the VideoSvc to live
    public static final String VIDEO_SVC_PATH = "/video";

    // The path to search videos by title
    public static final String VIDEO_TITLE_SEARCH_PATH = VIDEO_SVC_PATH + "/search/findByName";

    // The path to search videos by title
    public static final String VIDEO_DURATION_SEARCH_PATH = VIDEO_SVC_PATH + "/search/findByDurationLessThan";

    @Autowired
    private VideoRepository videoRepository;

    @RequestMapping(value= VIDEO_SVC_PATH, method= RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return Lists.newArrayList(videoRepository.findAll());
    }

    @RequestMapping(value= VIDEO_SVC_PATH + "/{id}", method= RequestMethod.GET)
    public @ResponseBody Video getVideoBy(@PathVariable("id") Long id) {
        Video video = videoRepository.findOne(id);

        if (video == null) throw new RequestNotFoundException();

        return video;
    }

    @RequestMapping(value= VIDEO_SVC_PATH, method= RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        return videoRepository.save(v);
    }

    @RequestMapping(value= VIDEO_TITLE_SEARCH_PATH , method= RequestMethod.GET)
    public @ResponseBody Collection<Video> findVideoByTitle(@RequestParam(TITLE_PARAMETER) String title) {
        return Lists.newArrayList(videoRepository.findByName(title));
    }

    @RequestMapping(value= VIDEO_DURATION_SEARCH_PATH , method= RequestMethod.GET)
    public @ResponseBody Collection<Video> findVideoByDurationLessThan(@RequestParam(DURATION_PARAMETER) Long duration) {
        return Lists.newArrayList(videoRepository.findByDurationLessThan(duration));
    }

    @RequestMapping(value = VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void likeVideo(@PathVariable("id") Long id, Principal principal)  {
        Video video = videoRepository.findOne(id);
        if (video == null) throw new RequestNotFoundException();
        boolean success = video.addUsername(principal.getName());
        if(!success) throw new BadRequestException();

        video.incrementLikes();
        videoRepository.save(video);
    }

    @RequestMapping(value = VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void unlikeVideo(@PathVariable("id") Long id, Principal principal)  {
        Video video = videoRepository.findOne(id);
        if (video == null) throw new RequestNotFoundException();

        boolean success = video.removeUsername(principal.getName());
        if(!success) throw new BadRequestException();

        video.decrementLikes();
        videoRepository.save(video);
    }

    @RequestMapping(value = VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
    public @ResponseBody List<String> getVideosLikedBy(@PathVariable("id") Long id) {
        Video video = videoRepository.findOne(id);
        if( video == null) throw new RequestNotFoundException();

        return new ArrayList<>(video.getUsernames());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private class RequestNotFoundException extends RuntimeException {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private class BadRequestException extends RuntimeException {
    }

}
