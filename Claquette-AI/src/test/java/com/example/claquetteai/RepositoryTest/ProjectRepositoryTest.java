package com.example.claquetteai.RepositoryTest;

import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryTest {

    @Mock
    ProjectRepository projectRepository;

    @Test
    void findProjectById_returnsStubbedProject() {
        Project stub = validProject(101, 7, "My Film");
        when(projectRepository.findProjectById(101)).thenReturn(stub);

        Project found = projectRepository.findProjectById(101);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(101, found.getId());
        Assertions.assertEquals("My Film", found.getTitle());
        Assertions.assertEquals("FILM", found.getProjectType());
        Assertions.assertEquals(7, found.getCompany().getId());
        verify(projectRepository).findProjectById(101);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void findProjectById_returnsNull_whenMissing() {
        when(projectRepository.findProjectById(9999)).thenReturn(null);

        Assertions.assertNull(projectRepository.findProjectById(9999));

        verify(projectRepository).findProjectById(9999);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void findProjectsByCompanyUserId_returnsList() {
        // company.user.id = 5
        Project p1 = validProject(201, 9, "Series A");  // company id doesn't matter for this test shape
        Project p2 = validProject(202, 9, "Series B");
        when(projectRepository.findProjectsByCompany_User_Id(5))
                .thenReturn(List.of(p1, p2));

        List<Project> found = projectRepository.findProjectsByCompany_User_Id(5);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(2, found.size());
        Assertions.assertEquals(201, found.get(0).getId());
        Assertions.assertEquals("Series A", found.get(0).getTitle());
        Assertions.assertEquals(202, found.get(1).getId());
        Assertions.assertEquals("Series B", found.get(1).getTitle());
        verify(projectRepository).findProjectsByCompany_User_Id(5);
        verifyNoMoreInteractions(projectRepository);
    }

    private static Project project(Integer id, String title, String type, Double budget) {
        Project p = new Project();
        p.setId(id);
        p.setTitle(title);
        p.setProjectType(type);
        p.setBudget(budget);
        return p;
    }

    private static Project validProject(Integer projectId, Integer companyId, String title) {
        Company c = new Company();
        c.setId(companyId);
        c.setName("MockCo");
        c.setCommercialRegNo("1234567890"); // exactly 10 chars per your constraint

        Project p = new Project();
        p.setId(projectId);
        p.setTitle(title);
        p.setDescription("desc");
        p.setProjectType("FILM"); // matches @Pattern
        p.setGenre("Drama");
        p.setBudget(1000.0);
        p.setLocation("Riyadh");
        p.setTargetAudience("Adults");
        p.setStatus("IN_DEVELOPMENT");
        p.setStartProjectDate(LocalDateTime.now().minusDays(10));
        p.setEndProjectDate(LocalDateTime.now().plusDays(10));
        p.setEpisodeCount(1);
        p.setCompany(c);
        return p;
    }
}
