package com.patho.main.service;

import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.DateFormat;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.Council;
import com.patho.main.repository.jpa.CouncilRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Service
@Transactional
public class CouncilService extends AbstractService {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private CouncilRepository councilRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private FavouriteListService favouriteListService;

    /**
     * Creates a new council
     *
     * @param task
     * @return
     */
    public CouncilReturn createCouncil(Task task, boolean addToFavList) {

        Council council = new Council(task);
        council.setDateOfRequest(LocalDate.now());
        council.setName(generateCouncilName(council));
        council.setAttachedPdfs(new HashSet<PDFContainer>());
        council.setNotificationMethod(Council.CouncilNotificationMethod.MTA);
        council.setTask(task);

        task.getCouncils().add(council);

        // task does not need to be saved, because council is mapped by the council.task
        // id;
        council = councilRepository.save(council,
                resourceBundle.get("log.patient.task.council.create", task, council.getName()), task.getPatient());

        task = favouriteListService.addTaskToList(council.getTask(), "", PredefinedFavouriteList.Council,
                PredefinedFavouriteList.CouncilRequest);

        return new CouncilReturn(task, council);
    }

    /**
     * Generates a council name
     *
     * @param council
     * @return
     */
    public String generateCouncilName(Council council) {
        StringBuffer str = new StringBuffer();

        // name
        if (council.getCouncilPhysician() != null)
            str.append(council.getCouncilPhysician().getPerson().getFullName());
        else
            str.append(resourceBundle.get("dialog.consultation.data.newCouncil"));

        str.append(" ");
        try {
            LocalDateTime ldt = council.getDateOfRequest().atStartOfDay();
            // adding date
            str.append(ldt.format(DateTimeFormatter.ofPattern(DateFormat.GERMAN_DATE.getDateFormat())));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        return str.toString();
    }

    /**
     * Ends council request phase
     *
     * @param council
     * @return
     */
    public CouncilReturn endCouncilRequest(Task task, Council council) {
        council.setCouncilRequestCompleted(true);
        council.setCouncilRequestCompletedDate(LocalDate.now());

        if (council.getNotificationMethod() == Council.CouncilNotificationMethod.SECRETARY)
            council.setSampleShippedCommentary(resourceBundle.get("dialog.consultation.sampleShipped.option.noSample"));

        council = councilRepository.save(council,
                resourceBundle.get("log.patient.task.council.phase.request.end", task, council.getName()),
                task.getPatient());

        task = favouriteListService.removeTaskFromList(council.getTask(), PredefinedFavouriteList.CouncilRequest);
        task = favouriteListService.addTaskToList(council.getTask(), PredefinedFavouriteList.CouncilWaitingForReply);

        if (council.getNotificationMethod() != Council.CouncilNotificationMethod.NONE) {
            task = favouriteListService.addTaskToList(task,
                    council.getNotificationMethod() == Council.CouncilNotificationMethod.MTA
                            ? PredefinedFavouriteList.CouncilSendRequestMTA
                            : PredefinedFavouriteList.CouncilSendRequestSecretary);

        }

        return new CouncilReturn(task, council);
    }

    public CouncilReturn beginReplyReceived(Task task, Council council) {
        task = favouriteListService.addTaskToList(council.getTask(), PredefinedFavouriteList.CouncilWaitingForReply);
        task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.CouncilReplyPresent);
        return new CouncilReturn(task, council);
    }

    public CouncilReturn endReplyReceived(Task task, Council council) {
        council.setReplyReceived(true);
        council.setReplyReceivedDate(LocalDate.now());

        council = councilRepository.save(council,
                resourceBundle.get("log.patient.task.council.phase.reply.received", task, council.getName()),
                task.getPatient());

        task = favouriteListService.removeTaskFromList(council.getTask(),
                PredefinedFavouriteList.CouncilWaitingForReply);
        // this is normally done via dumplist of councilWaitingForReply, this catches
        // errors
        task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.CouncilReplyPresent);

        return new CouncilReturn(task, council);
    }

    public CouncilReturn beginSampleShiped(Task task, Council council) {
        if (council.getNotificationMethod() != Council.CouncilNotificationMethod.NONE)
            task = favouriteListService.addTaskToList(council.getTask(),
                    council.getNotificationMethod() == Council.CouncilNotificationMethod.MTA
                            ? PredefinedFavouriteList.CouncilSendRequestMTA
                            : PredefinedFavouriteList.CouncilSendRequestSecretary);
        return new CouncilReturn(task, council);
    }

    public CouncilReturn endSampleShiped(Task task, Council council) {
        task = favouriteListService.removeTaskFromList(council.getTask(), PredefinedFavouriteList.CouncilSendRequestMTA,
                PredefinedFavouriteList.CouncilSendRequestSecretary);
        return new CouncilReturn(task, council);
    }

    public CouncilReturn endCouncil(Task task, Council council) {
        council.setCouncilCompleted(true);
        council.setCouncilCompletedDate(LocalDate.now());

        council = councilRepository.save(council,
                resourceBundle.get("log.patient.task.council.phase.end", task, council.getName()), task.getPatient());

        task = favouriteListService.removeTaskFromList(council.getTask(), false,
                PredefinedFavouriteList.CouncilWaitingForReply, PredefinedFavouriteList.CouncilReplyPresent,
                PredefinedFavouriteList.Council,
                PredefinedFavouriteList.CouncilRequest, PredefinedFavouriteList.CouncilSendRequestMTA,
                PredefinedFavouriteList.CouncilSendRequestSecretary, PredefinedFavouriteList.CouncilWaitingForReply);

        // this is normally done via dumplist of councilWaitingForReply, this catches
        // errors
        task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.CouncilCompleted);

        return new CouncilReturn(task, council);
    }

    public Task deleteCouncil(Task task, Council council) {
        task.getCouncils().remove(council);

        councilRepository.delete(council,
                resourceBundle.get("log.patient.task.council.deleted", task, council.getName()), task.getPatient());

        MessageHandler.sendGrowlMessagesAsResource("growl.consultation.deleted.headline", "growl.consultation.deleted.text");
        return task;
    }


    public static class CouncilReturn {
        protected Task task;
        protected Council council;

        public CouncilReturn(Task task, Council council) {
            this.task = task;
            this.council = council;
        }

        public Task getTask() {
            return this.task;
        }

        public Council getCouncil() {
            return this.council;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        public void setCouncil(Council council) {
            this.council = council;
        }
    }
}
