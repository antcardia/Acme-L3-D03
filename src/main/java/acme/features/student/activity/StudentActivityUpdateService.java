
package acme.features.student.activity;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.datatypes.Nature;
import acme.entities.enrolment.Activity;
import acme.entities.enrolment.Enrolment;
import acme.entities.system.SystemConfiguration;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import acme.roles.Student;
import antiSpamFilter.AntiSpamFilter;

@Service
public class StudentActivityUpdateService extends AbstractService<Student, Activity> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected StudentActivityRepository repository;

	// AbstractService<Employer, Company> -------------------------------------


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("id", int.class);

		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		final boolean status;
		final int masterId;
		final Integer activityId = super.getRequest().getData("id", int.class);
		final Enrolment enrolment;
		final Student student;
		final Activity activity = this.repository.findActivityById(activityId);
		status = activity.getEnrolment().getStudent().getId() == super.getRequest().getPrincipal().getActiveRoleId();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Activity object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.repository.findActivityByIdFinalised(id);

		super.getBuffer().setData(object);

	}

	@Override
	public void bind(final Activity object) {
		assert object != null;

		super.bind(object, "tittle", "abstract$", "workbookName", "startTime", "finishTime", "link");
		Nature atype;
		atype = super.getRequest().getData("atype", Nature.class);
		object.setAtype(atype);
	}

	@Override
	public void validate(final Activity object) {
		assert object != null;
		final SystemConfiguration config = this.repository.findSystemConfiguration();
		final AntiSpamFilter antiSpam = new AntiSpamFilter(config.getThreshold(), config.getSpamWords());
		if (!super.getBuffer().getErrors().hasErrors("tittle")) {
			final String motivation = object.getTittle();
			super.state(!antiSpam.isSpam(motivation), "tittle", "student.activity.form.error.spamTitle");
		}
		if (!super.getBuffer().getErrors().hasErrors("workbookName")) {
			final String goals = object.getWorkbookName();
			super.state(!antiSpam.isSpam(goals), "workbookName", "student.activity.form.error.spamTitle2");
		}
		if (!super.getBuffer().getErrors().hasErrors("abstract$")) {
			final String goals = object.getAbstract$();
			super.state(!antiSpam.isSpam(goals), "abstract$", "student.activity.form.error.spamTitle3");
		}
		if (!super.getBuffer().getErrors().hasErrors("startTime") && !super.getBuffer().getErrors().hasErrors("finishTime")) {
			final Date startTime = object.getStartTime();
			final Date finishTime = object.getFinishTime();
			super.state(startTime != MomentHelper.getCurrentMoment() && MomentHelper.isBefore(startTime, finishTime), "startTime", "student.activity.form.error.startTime");
			super.state(finishTime != MomentHelper.getCurrentMoment() && MomentHelper.isBefore(startTime, finishTime), "finishTime", "student.activity.form.error.finishTime");
		}
		if (!super.getBuffer().getErrors().hasErrors("lectureType"))
			super.state(!object.getAtype().equals(Nature.BALANCED), "atype", "student.activity.form.error.atype");

		if (!super.getBuffer().getErrors().hasErrors("link"))
			super.state(object.getLink().length() < 255, "link", "student.activity.form.error.outOfRangeLink");
	}

	@Override
	public void perform(final Activity object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final Activity object) {
		assert object != null;

		Tuple tuple;

		tuple = super.unbind(object, "tittle", "abstract$", "workbookName", "startTime", "finishTime", "link");

		final SelectChoices choices = SelectChoices.from(Nature.class, object.getAtype());
		tuple.put("atype", choices.getSelected().getKey());
		tuple.put("activityType", choices);

		super.getResponse().setData(tuple);
	}

}
