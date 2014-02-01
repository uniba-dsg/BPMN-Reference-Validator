package de.uniba.wiai.lspi.ws1213.ba.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidator;
import de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl;
import de.uniba.wiai.lspi.ws1213.ba.application.Violation;
import de.uniba.wiai.lspi.ws1213.ba.application.TypeViolation;
import de.uniba.wiai.lspi.ws1213.ba.application.ValidationResult;
import de.uniba.wiai.lspi.ws1213.ba.application.ValidatorException;

/**
 * This test class tests the validator with the predefined BPMN files. The
 * predefinitions of the tests can be looked up in the bachelor thesis with the
 * T numbers. The tests are jUnit tests.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * 
 */
public class TestBPMNFiles {

	private static BPMNReferenceValidator application;

	@BeforeClass
	public static void setupBeforeClass() throws ValidatorException {
		application = new BPMNReferenceValidatorImpl();
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T1.
	 * 
	 * @throws ValidatorException
	 */
	@Test
	public void testValidateWithT1() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-1-gruppe-c.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("messageFlow", 11,
				"messageRef", "participant", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T2.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT2() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-2-gruppe-d.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("message", 3,
				"itemRef", "participant", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T3.
	 * 
	 * @throws ValidatorException
	 */
	@Test
	public void testValidateWithT3() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-3-gruppe-e.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(2, errors.size());
		TypeViolation foundError1 = (TypeViolation) errors.get(0);
		TypeViolation expectedError1 = new TypeViolation("sequenceFlow", 32,
				"sourceRef", "participant", null, null);
		TypeViolation foundError2 = (TypeViolation) errors.get(1);
		TypeViolation expectedError2 = new TypeViolation("sequenceFlow", 33,
				"targetRef", "sequenceFlow", null, null);
		assertEquals(expectedError1, foundError1);
		assertEquals(expectedError2, foundError2);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T4.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT4() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-4-gruppe-f.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(2, errors.size());
		TypeViolation foundError1 = (TypeViolation) errors.get(0);
		TypeViolation expectedError1 = new TypeViolation("task", 10,
				"incoming", "participant", null, null);
		TypeViolation foundError2 = (TypeViolation) errors.get(1);
		TypeViolation expectedError2 = new TypeViolation("exclusiveGateway",
				30, "default", "startEvent", null, null);
		assertEquals(expectedError1, foundError1);
		assertEquals(expectedError2, foundError2);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T5.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT5() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-5-gruppe-g.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("participant", 11,
				"interfaceRef", "task", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T6.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT6() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-6-gruppe-h.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("partnerEntity", 23,
				"participantRef", "sequenceFlow", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T7.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT7() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-7-gruppe-i1.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(2, errors.size());
		TypeViolation foundError1 = (TypeViolation) errors.get(0);
		TypeViolation expectedError1 = new TypeViolation("messageFlow", 11,
				"sourceRef", "message", null, null);
		TypeViolation foundError2 = (TypeViolation) errors.get(1);
		TypeViolation expectedError2 = new TypeViolation("messageFlow", 11,
				"targetRef", "process", null, null);
		assertEquals(expectedError1, foundError1);
		assertEquals(expectedError2, foundError2);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T8.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT8() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-8-gruppe-l.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("sendTask", 22,
				"operationRef", "message", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T9.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT9() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-9-gruppe-m.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("inputSet", 18,
				"dataInputRefs", "participant", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T10.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT10() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-10-gruppe-n.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("outputSet", 19,
				"dataOutputRefs", "participant", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T11.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT11() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-11-gruppe-o.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(2, errors.size());
		TypeViolation foundError1 = (TypeViolation) errors.get(0);
		TypeViolation expectedError1 = new TypeViolation(
				"dataOutputAssociation", 23, "sourceRef", "participant", null,
				null);
		TypeViolation foundError2 = (TypeViolation) errors.get(1);
		TypeViolation expectedError2 = new TypeViolation(
				"dataInputAssociation", 44, "targetRef", "startEvent", null,
				null);
		assertEquals(expectedError1, foundError1);
		assertEquals(expectedError2, foundError2);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T12.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT12() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-12-gruppe-p.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation(
				"intermediateThrowEvent", 27, "eventDefinitionRef",
				"sequenceFlow", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T13.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT13() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-13-gruppe-q.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("boundaryEvent", 19,
				"attachedToRef", "participant", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T14.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT14() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-14-gruppe-r.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(2, errors.size());
		TypeViolation foundError1 = (TypeViolation) errors.get(0);
		TypeViolation expectedError1 = new TypeViolation("linkEventDefinition",
				24, "source", "startEvent", null, null);
		TypeViolation foundError2 = (TypeViolation) errors.get(1);
		TypeViolation expectedError2 = new TypeViolation("linkEventDefinition",
				43, "target", "endEvent", null, null);
		assertEquals(expectedError1, foundError1);
		assertEquals(expectedError2, foundError2);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T15.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT15() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-15-gruppe-s.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("process", 18,
				"definitionalCollaborationRef", "message", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T16.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT16() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-16-gruppe-t.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("correlationKey", 23,
				"correlationPropertyRef", "participant", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T17.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT17() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-17-gruppe-u.bpmn");
		assertEquals(1, results.size());
		ValidationResult result = results.get(0);
		assertFalse(result.isValid());
		List<Violation> errors = result.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("operation", 8,
				"errorRef", "message", null, null);
		assertEquals(expectedError, foundError);
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T18.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT18() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-18-referenz-6-teil-2.bpmn");
		assertEquals(2, results.size());
		ValidationResult result1 = results.get(0);
		assertFalse(result1.isValid());
		List<Violation> errors = result1.getViolations();
		assertEquals(1, errors.size());
		TypeViolation foundError = (TypeViolation) errors.get(0);
		TypeViolation expectedError = new TypeViolation("group", 10,
				"categoryValueRef", "participant", null, null);
		assertEquals(expectedError, foundError);
		assertTrue(results.get(1).isValid());
	}

	/**
	 * Test method for
	 * {@link de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl#validate(java.lang.String)}
	 * with T19.
	 * 
	 * @throws ValidatorException
	 * 
	 */
	@Test
	public void testValidateWithT19() throws ValidatorException {
		List<ValidationResult> results = application
				.validate("tests/test-19-referenz-6-korrekt.bpmn");
		assertEquals(2, results.size());
		assertTrue(results.get(0).isValid());
		assertTrue(results.get(1).isValid());
	}
}
