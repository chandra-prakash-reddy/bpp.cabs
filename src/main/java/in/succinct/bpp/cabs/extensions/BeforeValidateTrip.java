package in.succinct.bpp.cabs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelValidateExtension;
import com.venky.swf.plugins.background.core.Task;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.routing.Config;
import in.succinct.beckn.Context;
import in.succinct.beckn.Message;
import in.succinct.beckn.Request;
import in.succinct.becknify.client.Becknify;
import in.succinct.bpp.cabs.controller.BecknController;
import in.succinct.bpp.cabs.db.model.demand.RejectedTrip;
import in.succinct.bpp.cabs.db.model.demand.Trip;

public class BeforeValidateTrip extends BeforeModelValidateExtension<Trip> {
    static{
        registerExtension(new BeforeValidateTrip());
    }
    @Override
    public void beforeValidate(Trip model) {
        if (!isStatusChangeApproved(model)){
            throw new UnsupportedOperationException();
        }
        if (model.getRawRecord().isFieldDirty("DRIVER_ACCEPTANCE_STATUS") || model.getRawRecord().isFieldDirty("STATUS") ){
            if (Trip.STATUS_LIST.indexOf(model.getStatus()) > 0){
                TaskManager.instance().executeAsync((Task)()-> model.notifyBap(),false);
            }
        }
        if (!model.getRawRecord().isNewRecord() && model.getRawRecord().isFieldDirty("DRIVER_ACCEPTANCE_STATUS") ){
            if (ObjectUtil.equals(model.getDriverAcceptanceStatus(), Trip.Rejected )){
                if (model.getDriverLoginId() != null) {
                    RejectedTrip rejectedTrip = Database.getTable(RejectedTrip.class).newRecord();
                    rejectedTrip.setTripId(model.getId());
                    rejectedTrip.setDriverLoginId(model.getDriverLoginId());
                    rejectedTrip.save();
                }
                model.setDriverLoginId(null);
                TaskManager.instance().executeAsync((Task)()-> model.allocate(),false);
            }else if (ObjectUtil.equals(model.getDriverAcceptanceStatus(),Trip.Accepted)){
                for (Trip t : model.getDriverLogin().getTrips()) {
                    if (t.getStartTs() == null && t.getId() != model.getId() && ObjectUtil.equals(t.getStatus(), Trip.Confirmed) && !ObjectUtil.equals(t.getDriverAcceptanceStatus(),Trip.Accepted)) {
                        TaskManager.instance().executeAsync((Task) () -> t.reject(), false);
                    }
                }
            }
        }

    }
    public boolean isStatusChangeApproved(Trip trip){
        if (trip.getRawRecord().isFieldDirty("STATUS")){
            String oldStatus = (String)trip.getRawRecord().getOldValue("STATUS");
            String newStatus = trip.getStatus();
            return (Trip.STATUS_LIST.indexOf(oldStatus) <= Trip.STATUS_LIST.indexOf(newStatus));
        }
        return true;
    }
}
