package kz.mlapp.repos;



import kz.mlapp.model.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStatusRepository extends JpaRepository<FileStatus, Long> {

}
