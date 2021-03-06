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
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static org.magnum.dataup.VideoSvcApi.*;

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

    @Autowired
    private VideoRepository videoRepository;

    private VideoFileManager videoFileManager;

    //GET /video - Returns the list of videos that have been added to the server as JSON. The list of videos does not have to be persisted across restarts of the server. The list of Video objects should be able to be unmarshalled by the client into a Collection . - The return content-type should be application/json, which will be the default if you use @ResponseBody
    //POST /video - The video metadata is provided as an application/json request body. The JSON should generate a valid instance of the Video class when deserialized by Spring's default Jackson library. - Returns the JSON representation of the Video object that was stored along with any updates to that object made by the server. - The server should generate a unique identifier for the Video object and assign it to the Video by calling its setId(...) method. - No video should have ID = 0. All IDs should be > 0. - The returned Video JSON should include this server-generated identifier so that the client can refer to it when uploading the binary mpeg video content for the Video. - The server should also generate a "data url" for the Video. The "data url" is the url of the binary data for a Video (e.g., the raw mpeg data). The URL should be the full URL for the video and not just the path (e.g., http://localhost:8080/video/1/data would be a valid data url). See the Hints section for some ideas on how to generate this URL.
    //POST /video/{id}/data - The binary mpeg data for the video should be provided in a multipart request as a part with the key "data". The id in the path should be replaced with the unique identifier generated by the server for the Video. A client MUSTcreate* a Video first by sending a POST to /video and getting the identifier for the newly created Video object before sending a POST to /video/{id}/data. - The endpoint should return a VideoStatus object with state=VideoState.READY if the request succeeds and the appropriate HTTP error status otherwise. VideoState.PROCESSING is not used in this assignment but is present in VideoState. - Rather than a PUT request, a POST is used because, by default, Spring does not support a PUT with multipart data due to design decisions in the Commons File Upload library: https://issues.apache.org/jira/browse/FILEUPLOAD-197
    //GET /video/{id}/data - Returns the binary mpeg data (if any) for the video with the given identifier. If no mpeg data has been uploaded for the specified video, then the server should return a 404 status code.


    public VideoController() throws IOException {
        videoFileManager = VideoFileManager.get();
    }

    @RequestMapping(value= VIDEO_SVC_PATH, method= RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return videoRepository.getVideos();
    }

    @RequestMapping(value= VIDEO_SVC_PATH, method= RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        videoRepository.save(v);
        String dataUrl = String.format("%s/video/%d/data", getUrlBaseForLocalServer(), v.getId());
        v.setDataUrl(dataUrl);
        videoRepository.save(v);
        return v;
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method= RequestMethod.POST)
    public @ResponseBody VideoStatus setVideoData(@PathVariable(ID_PARAMETER) Long id, @RequestParam(DATA_PARAMETER) MultipartFile videoData)
            throws IOException {
        if (!videoData.isEmpty()) {
            Video videoWithId = videoRepository.getVideoWithId(id);
            if( videoWithId == null)  throw new RequestNotFoundException();
            videoFileManager.saveVideoData(videoWithId, videoData.getInputStream());
            return new VideoStatus(VideoStatus.VideoState.READY);
        }

        return null;
    }

    @RequestMapping(value=VIDEO_DATA_PATH, method= RequestMethod.GET)
    public void getData(@PathVariable(ID_PARAMETER) Long id, HttpServletResponse response) throws IOException {
        Video videoWithId = videoRepository.getVideoWithId( id );
        if( videoWithId == null || !videoFileManager.hasVideoData(videoWithId))  throw new RequestNotFoundException();

        response.setContentType(videoWithId.getContentType());
        videoFileManager.copyVideoData(videoWithId, response.getOutputStream());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private class RequestNotFoundException extends RuntimeException {
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "http://"+request.getServerName()
                + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
    }
}
