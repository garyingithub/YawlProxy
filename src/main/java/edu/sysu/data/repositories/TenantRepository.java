package edu.sysu.data.repositories;

import edu.sysu.data.Tenant;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by gary on 16-8-7.
 */
public interface TenantRepository extends CrudRepository<Tenant,Long> {
}
