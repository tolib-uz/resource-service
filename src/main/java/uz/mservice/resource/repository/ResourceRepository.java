package uz.mservice.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mservice.resource.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

}
