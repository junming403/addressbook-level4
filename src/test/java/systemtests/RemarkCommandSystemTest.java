package systemtests;

import org.junit.Test;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.Model;
import seedu.address.model.module.Code;
import seedu.address.model.module.Remark;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_DISPLAYED_INDEX;
import static seedu.address.logic.commands.RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS;
import static seedu.address.logic.commands.RemarkCommand.MESSAGE_REMARK_MODULE_SUCCESS;
import static seedu.address.logic.commands.RemarkCommand.MESSAGE_WRONG_LISTING_UNIT_FAILURE;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_LESSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_LESSON;

public class RemarkCommandSystemTest extends AddressBookSystemTest {

    private static final String SAMPLE_REMARK = "This is a sample remark";
    private static final String MESSSAGE_DUPLICATE_REMARK = "Operation would result in duplicate remark";
    private static final String MESSAGE_INVALID_REMARK_COMMAND_FORMAT =
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

    @Test
    public void remark() throws Exception {
        /* list by module */
        String listModuleCommand = ListCommand.COMMAND_WORD + " module";
        executeCommand(listModuleCommand);

        /* Case:remark the first module in the list, command with leading spaces and trailing spaces -> remarked*/
        Model expectedModel = getModel();
        String command = "     " + RemarkCommand.COMMAND_WORD + "      " + INDEX_FIRST_LESSON.getOneBased() + "       "
                + SAMPLE_REMARK;
        Code moduleToRemark = expectedModel.getFilteredLessonList().get(INDEX_FIRST_LESSON.getZeroBased()).getCode();
        expectedModel.addRemark(new Remark(SAMPLE_REMARK, moduleToRemark));
        String expectedResultMessage = String.format(MESSAGE_REMARK_MODULE_SUCCESS, moduleToRemark);
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case:remark the second module in the list -> remarked*/
        expectedModel = getModel();
        command = RemarkCommand.COMMAND_WORD + " " + INDEX_SECOND_LESSON.getOneBased() + "  " + SAMPLE_REMARK;
        moduleToRemark = expectedModel.getFilteredLessonList().get(INDEX_SECOND_LESSON.getZeroBased()).getCode();
        expectedModel.addRemark(new Remark(SAMPLE_REMARK, moduleToRemark));
        expectedResultMessage = String.format(MESSAGE_REMARK_MODULE_SUCCESS, moduleToRemark);
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case:delete the second remark in the list -> deleted*/
        expectedModel = getModel();
        command = RemarkCommand.COMMAND_WORD + " -d " + INDEX_SECOND_LESSON.getOneBased();
        Remark remarkToDelete = expectedModel.getFilteredRemarkList().get(INDEX_SECOND_LESSON.getZeroBased());
        expectedModel.deleteRemark(remarkToDelete);
        expectedResultMessage = String.format(MESSAGE_DELETE_REMARK_SUCCESS, remarkToDelete);
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case: Undo the previous step -> Undoed */
        expectedModel = getModel();
        command = UndoCommand.COMMAND_WORD;
        expectedModel.addRemark(new Remark(SAMPLE_REMARK, moduleToRemark));
        expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case: redo the last command -> redoed */
        expectedModel = getModel();
        command = RedoCommand.COMMAND_WORD;
        expectedModel.deleteRemark(remarkToDelete);
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case: add in the same remark again to the first module, duplicate remark -> rejected */
        command = RemarkCommand.COMMAND_WORD + " " + INDEX_FIRST_LESSON.getOneBased() + " " + SAMPLE_REMARK;
        assertCommandFailure(command, MESSSAGE_DUPLICATE_REMARK);

        /* Case: invalid module index (0) -> rejected */
        command = RemarkCommand.COMMAND_WORD + " 0 " + SAMPLE_REMARK;
        assertCommandFailure(command, MESSAGE_INVALID_REMARK_COMMAND_FORMAT);

        /* Case: invalid module index (-1) -> rejected */
        command = RemarkCommand.COMMAND_WORD + " -1 " + SAMPLE_REMARK;
        assertCommandFailure(command, MESSAGE_INVALID_REMARK_COMMAND_FORMAT);

        /* Case: invalid module index (size + 1) -> rejected */
        Index outOfBoundsIndex = Index.fromOneBased(
                getModel().getAddressBook().getLessonList().size() + 1);
        command = RemarkCommand.COMMAND_WORD + " " + outOfBoundsIndex.getOneBased() + " " + SAMPLE_REMARK;
        assertCommandFailure(command, MESSAGE_INVALID_DISPLAYED_INDEX);

        /* Case: invalid remark index (0) -> rejected */
        command = RemarkCommand.COMMAND_WORD + " -d 0";
        assertCommandFailure(command, MESSAGE_INVALID_REMARK_COMMAND_FORMAT);

        /* Case: invalid remark index (-1) -> rejected */
        command = RemarkCommand.COMMAND_WORD + " -d -1 " + SAMPLE_REMARK;
        assertCommandFailure(command, MESSAGE_INVALID_REMARK_COMMAND_FORMAT);

        /* Case: invalid remark index (size + 1) -> rejected */
        outOfBoundsIndex = Index.fromOneBased(
                getModel().getAddressBook().getRemarkList().size() + 1);
        command = RemarkCommand.COMMAND_WORD + " -d " + outOfBoundsIndex.getOneBased();
        assertCommandFailure(command, MESSAGE_INVALID_DISPLAYED_INDEX);

        /* Case: add remark when list by lessons, wrong listing type -> rejected */
        command = ViewCommand.COMMAND_WORD + " 1";
        executeCommand(command);
        command = RemarkCommand.COMMAND_WORD + " 1  another remark";
        assertCommandFailure(command, MESSAGE_WRONG_LISTING_UNIT_FAILURE);

        /* Case: delete remark when list by lessons, wrong listing type -> rejected */
        command = RemarkCommand.COMMAND_WORD + " -d 1";
        assertCommandFailure(command, MESSAGE_WRONG_LISTING_UNIT_FAILURE);

        /* Case: add remark when list by location, wrong listing type -> rejected */
        command = ListCommand.COMMAND_WORD + " location";
        executeCommand(command);
        command = RemarkCommand.COMMAND_WORD + " 1  another remark";
        assertCommandFailure(command, MESSAGE_WRONG_LISTING_UNIT_FAILURE);

        /* Case: delete remark when list by location, wrong listing type -> rejected */
        command = RemarkCommand.COMMAND_WORD + " -d 1";
        assertCommandFailure(command, MESSAGE_WRONG_LISTING_UNIT_FAILURE);
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays an empty string.<br>
     * 2. Asserts that the result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the model related components equal to {@code expectedModel}.<br>
     * 4. Asserts that the status bar's sync status changes.<br>
     * 5. Asserts that the command box has the default style class.<br>
     * Verifications 1 to 3 are performed by
     * {@code AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.
     * @see AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and verifies that the command box displays {@code command}, the result display
     * box displays {@code expectedResultMessage} and the model related components equal to the current model.
     * These verifications are done by
     * {@code AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * Also verifies that the browser url, selected card and status bar remain unchanged, and the command box has the
     * error style.
     * @see AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();
        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }

}
