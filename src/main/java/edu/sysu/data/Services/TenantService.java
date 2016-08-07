package edu.sysu.data.Services;

import edu.sysu.data.Tenant;
import edu.sysu.data.repositories.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by gary on 16-8-7.
 */
@Service
public class TenantService  {

    private Logger logger= LoggerFactory.getLogger(this.getClass());


    @Resource
    private TenantRepository tenantRepository;

    @Transactional
    public Tenant create(String name){
        return tenantRepository.save(new Tenant(name));
    }

    @Transactional(rollbackFor = TenantNotFoundException.class)
    
    public Tenant delete(Long tenantId) throws TenantNotFoundException {
        logger.debug("Deleting tenant with id: " + tenantId);

        Tenant deleted = tenantRepository.findOne(tenantId);

        if (deleted == null) {
            logger.debug("No tenant found with id: " + tenantId);
            throw new TenantNotFoundException();
        }

        tenantRepository.delete(deleted);
        return deleted;
    }

    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        logger.debug("Finding all tenants");
        return (List<Tenant>) tenantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tenant findById(Long id) {
        logger.debug("Finding tenant by id: " + id);
        return tenantRepository.findOne(id);
    }

    @Transactional(rollbackFor = TenantNotFoundException.class)
    public Tenant update(Tenant updated) throws TenantNotFoundException {
        logger.debug("Updating tenant with information: " + updated);

        Tenant tenant = tenantRepository.findOne(updated.getTenantId());

        if (tenant == null) {
            logger.debug("No tenant found with id: " + updated.getTenantId());
            throw new TenantNotFoundException();
        }

        tenant.setName(updated.getName());

        return tenant;
    }

    /**
     * This setter method should be used only by unit tests.
     * @param tenantRepository
     */
    public void setTenantRepository(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }


    public static class TenantNotFoundException extends Exception{

    }
}
