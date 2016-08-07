/**
 * Created by gary on 16-8-7.
 */
import edu.sysu.data.Case;
import edu.sysu.data.Services.TenantService;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.data.repositories.TenantRepository;

import static org.mockito.Mockito.mock;

public class test {
    public static void main(String args[]){


        TenantService tenantService=new TenantService();

        tenantService.setTenantRepository(mock(TenantRepository.class));
        tenantService.create("try");


    }
}
