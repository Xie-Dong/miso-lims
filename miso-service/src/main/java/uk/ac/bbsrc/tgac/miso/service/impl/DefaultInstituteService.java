package uk.ac.bbsrc.tgac.miso.service.impl;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.User;
import com.google.common.collect.Sets;

import uk.ac.bbsrc.tgac.miso.core.data.Institute;
import uk.ac.bbsrc.tgac.miso.core.store.DeletionStore;
import uk.ac.bbsrc.tgac.miso.persistence.InstituteDao;
import uk.ac.bbsrc.tgac.miso.service.InstituteService;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationError;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationResult;
import uk.ac.bbsrc.tgac.miso.service.security.AuthorizationManager;

@Transactional(rollbackFor = Exception.class)
@Service
public class DefaultInstituteService implements InstituteService {
  
  protected static final Logger log = LoggerFactory.getLogger(DefaultInstituteService.class);
  
  @Autowired
  private InstituteDao instituteDao;
  
  @Autowired
  private DeletionStore deletionStore;

  @Autowired
  private AuthorizationManager authorizationManager;

  @Override
  public Institute get(long id) throws IOException {
    authorizationManager.throwIfUnauthenticated();
    return instituteDao.getInstitute(id);
  }

  @Override
  public Long create(Institute institute) throws IOException {
    authorizationManager.throwIfNotInternal();
    User user = authorizationManager.getCurrentUser();
    institute.setCreatedBy(user);
    institute.setUpdatedBy(user);
    return instituteDao.addInstitute(institute);
  }

  @Override
  public void update(Institute institute) throws IOException {
    authorizationManager.throwIfNonAdmin();
    Institute updatedInstitute = get(institute.getId());
    updatedInstitute.setAlias(institute.getAlias());
    User user = authorizationManager.getCurrentUser();
    updatedInstitute.setUpdatedBy(user);
    instituteDao.update(updatedInstitute);
  }

  @Override
  public Set<Institute> getAll() throws IOException {
    authorizationManager.throwIfUnauthenticated();
    return Sets.newHashSet(instituteDao.getInstitute());
  }

  @Override
  public DeletionStore getDeletionStore() {
    return deletionStore;
  }

  @Override
  public AuthorizationManager getAuthorizationManager() {
    return authorizationManager;
  }

  @Override
  public ValidationResult validateDeletion(Institute object) {
    ValidationResult result = new ValidationResult();

    long usage = instituteDao.getUsage(object);
    if (usage > 0L) {
      result.addError(new ValidationError(usage + " lab" + (usage > 1L ? "s are" : " is") + " associated with institute '"
          + object.getAlias() + "'"));
    }

    return result;
  }

}
