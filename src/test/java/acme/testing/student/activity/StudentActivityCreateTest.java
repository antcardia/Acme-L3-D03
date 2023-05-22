/*
 * EmployerJobCreateTest.java
 *
 * Copyright (C) 2012-2023 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.testing.student.activity;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;

import acme.entities.enrolment.Enrolment;
import acme.testing.TestHarness;
import acme.testing.student.enrolment.StudentEnrolmentTestRepository;

public class StudentActivityCreateTest extends TestHarness {

	@Autowired
	protected StudentEnrolmentTestRepository repository;


	@ParameterizedTest
	@CsvFileSource(resources = "/student/enrolment/create-positive.csv", encoding = "utf-8", numLinesToSkip = 1)
	public void test100Positive(final int recordIndex, final String tittle, final String abstract$, final String workbookName, final String atype, final String startTime, final String finishTime, final String link) {
		// HINT: this test authenticates as an employer and then lists his or her
		// HINT: jobs, creates a new one, and check that it's been created properly.

		super.signIn("student1", "student1");

		super.clickOnMenu("Student", "My enrolments");
		super.sortListing(0, "asc");
		super.clickOnListingRecord(recordIndex);
		super.checkFormExists();
		super.clickOnButton("Activities");
		super.clickOnListingRecord(recordIndex);

		super.clickOnButton("Create");
		super.fillInputBoxIn("tittle", tittle);
		super.fillInputBoxIn("abstract$", abstract$);
		super.fillInputBoxIn("workbookName", workbookName);
		super.fillInputBoxIn("atype", atype);
		super.fillInputBoxIn("startTime", startTime);
		super.fillInputBoxIn("finishTime", finishTime);
		super.fillInputBoxIn("link", link);
		super.clickOnSubmit("Create");

		super.clickOnMenu("Student", "My enrolments");
		super.sortListing(0, "asc");
		super.clickOnListingRecord(recordIndex);
		super.checkFormExists();
		super.clickOnButton("Activities");
		super.clickOnListingRecord(recordIndex);
		super.checkColumnHasValue(recordIndex, 0, tittle);
		super.checkColumnHasValue(recordIndex, 1, abstract$);

		super.clickOnListingRecord(recordIndex);
		super.checkFormExists();
		super.checkInputBoxHasValue("tittle", tittle);
		super.checkInputBoxHasValue("abstract$", abstract$);
		super.checkInputBoxHasValue("workbookName", workbookName);
		super.checkInputBoxHasValue("atype", atype);
		super.checkInputBoxHasValue("startTime", startTime);
		super.checkInputBoxHasValue("finishTime", finishTime);
		super.checkInputBoxHasValue("link", link);

		super.signOut();
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/student/enrolment/create-negative.csv", encoding = "utf-8", numLinesToSkip = 1)
	public void test200Negative(final int recordIndex, final String tittle, final String abstract$, final String workbookName, final String atype, final String startTime, final String finishTime, final String link) {
		// HINT: this test attempts to create jobs with incorrect data.

		super.signIn("student1", "student1");

		super.clickOnMenu("Student", "My enrolments");
		super.sortListing(0, "asc");
		super.clickOnListingRecord(recordIndex);
		super.checkFormExists();
		super.clickOnButton("Activities");
		super.clickOnListingRecord(recordIndex);

		super.fillInputBoxIn("tittle", tittle);
		super.fillInputBoxIn("abstract$", abstract$);
		super.fillInputBoxIn("workbookName", workbookName);
		super.fillInputBoxIn("atype", atype);
		super.fillInputBoxIn("startTime", startTime);
		super.fillInputBoxIn("finishTime", finishTime);
		super.fillInputBoxIn("link", link);

		super.clickOnSubmit("Create");

		super.checkErrorsExist();

		super.signOut();
	}

	@Test
	public void test300Hacking() {

		Collection<Enrolment> enrolments;
		String param;

		enrolments = this.repository.findManyEnrolmentsByStudentUsername("student1");
		for (final Enrolment e : enrolments) {
			param = String.format("id=%d", e.getId());

			super.checkLinkExists("Sign in");
			super.request("/student/activity/show", param);
			super.checkPanicExists();

			super.signIn("administrator", "administrator");
			super.request("/employer/job/show", param);
			super.checkPanicExists();
			super.signOut();

			super.signIn("student2", "student2");
			super.request("/student/activity/show", param);
			super.checkPanicExists();
			super.signOut();

			super.signIn("lecturer1", "lecturer1");
			super.request("/student/activity/show", param);
			super.checkPanicExists();
			super.signOut();
		}
	}

}
