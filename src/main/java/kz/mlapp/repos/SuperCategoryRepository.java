package kz.mlapp.repos;

import kz.mlapp.model.SuperCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuperCategoryRepository extends JpaRepository<SuperCategory, Long> {


    Optional<SuperCategory> findByName(String name);
}
