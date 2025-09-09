package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Project findProjectById(Integer id);

    List<Project> findProjectsByCompany_User_Id(Integer companyUserId);
    @Query("""
   select p from Project p
   left join fetch p.characters
   where p.id = :id
""")
    Optional<Project> findByIdWithCharacters(Integer id);

    @Query("""
   select p from Project p
   left join fetch p.episodes e
   left join fetch e.scenes s
   left join fetch s.characters sc
   where p.id = :id
""")
    Optional<Project> findByIdWithEpisodesScenesAndCharacters(Integer id);
}
