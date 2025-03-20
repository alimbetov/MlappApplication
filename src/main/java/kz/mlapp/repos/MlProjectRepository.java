package kz.mlapp.repos;

import kz.mlapp.model.MlProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MlProjectRepository extends JpaRepository<MlProject, Long> {

}
