package kz.mlapp.repos;

import kz.mlapp.model.FileBox;
import kz.mlapp.model.FileStatus;
import kz.mlapp.model.MlProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileBoxRepository extends JpaRepository<FileBox, Long> {

    Page<FileBox> findAllByProjectAndStatus(MlProject project, FileStatus status, Pageable pageable);
}
