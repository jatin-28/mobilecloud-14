package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: jatin
 */
@Component
public class VideoRepository {
    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long,Video> videos = new ConcurrentHashMap<>();

    public Video getVideoWithId(Long id) {
        return videos.get(id);
    }

    public Collection<Video> getVideos() {
        return videos.values();
    }

    public Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }

}
