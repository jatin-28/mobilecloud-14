package org.magnum.mobilecloud.video.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{

    // Find all videos with a matching title (e.g., Video.name)
    public Collection<Video> findByName(String title);
	
	// Find all videos that are shorter than a specified duration
	public Collection<Video> findByDurationLessThan(long maxduration);


	/*
	 * See: http://docs.spring.io/spring-data/jpa/docs/1.3.0.RELEASE/reference/html/jpa.repositories.html 
	 * for more examples of writing query methods
	 */
	
}
