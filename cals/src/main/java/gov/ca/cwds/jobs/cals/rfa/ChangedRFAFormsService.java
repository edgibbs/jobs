package gov.ca.cwds.jobs.cals.rfa;

import com.google.inject.Inject;
import gov.ca.cwds.cals.persistence.dao.calsns.RFA1aFormsDao;
import gov.ca.cwds.cals.service.mapper.RFA1aFormMapper;
import gov.ca.cwds.jobs.common.RecordChangeOperation;
import gov.ca.cwds.jobs.common.job.ChangedEntitiesService;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Created by Alexander Serbin on 1/30/2018.
 */
public class ChangedRFAFormsService implements ChangedEntitiesService<ChangedRFA1aFormDTO> {

    @Inject
    private RFA1aFormsDao dao;

    @Inject
    private RFA1aFormMapper rfa1aFormMapper;

    private Stream<ChangedRFA1aFormDTO> streamChangedRFA1aForms(LocalDateTime after) {
        return dao.streamChangedRFA1aForms(after).map(
                rfa1aForm -> new ChangedRFA1aFormDTO(rfa1aFormMapper.toExpandedRFA1aFormDTO(rfa1aForm),
                        RecordChangeOperation.I));
    }


    @Override
    public Stream<ChangedRFA1aFormDTO> doInitialLoad() {
        return streamChangedRFA1aForms(LocalDateTime.now().minusYears(100));
    }

    @Override
    public Stream<ChangedRFA1aFormDTO> doIncrementalLoad(LocalDateTime dateAfter) {
        return streamChangedRFA1aForms(dateAfter);
    }
}
