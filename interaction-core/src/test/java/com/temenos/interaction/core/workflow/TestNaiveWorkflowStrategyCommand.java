package com.temenos.interaction.core.workflow;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionCommand.Result;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;

public class TestNaiveWorkflowStrategyCommand {

	@Test
	public void testAllCommandsExecutedConstructor() throws InteractionException {
		InteractionCommand command1 = mock(InteractionCommand.class);
		InteractionCommand command2 = mock(InteractionCommand.class);
		InteractionContext ctx = mock(InteractionContext.class);
		
		List<InteractionCommand> commands = new ArrayList<InteractionCommand>();
		commands.add(command1);
		commands.add(command2);
		NaiveWorkflowStrategyCommand w = new NaiveWorkflowStrategyCommand(commands);
		
		w.execute(ctx);
		verify(command1, times(1)).execute(ctx);
		verify(command2, times(1)).execute(ctx);
	}

	@Test
	public void testAllCommandsExecuted() throws InteractionException {
		InteractionCommand command1 = mock(InteractionCommand.class);
		InteractionCommand command2 = mock(InteractionCommand.class);
		InteractionContext ctx = mock(InteractionContext.class);
		
		NaiveWorkflowStrategyCommand w = new NaiveWorkflowStrategyCommand();
		w.addCommand(command1);
		w.addCommand(command2);
		
		w.execute(ctx);
		verify(command1, times(1)).execute(ctx);
		verify(command2, times(1)).execute(ctx);
	}

	@Test
	public void testAllCommandsExecutedWhereOneFails() throws InteractionException {
		InteractionCommand command1 = mock(InteractionCommand.class);
		when(command1.execute(any(InteractionContext.class))).thenReturn(Result.FAILURE);
		InteractionCommand command2 = mock(InteractionCommand.class);
		when(command2.execute(any(InteractionContext.class))).thenReturn(Result.SUCCESS);
		InteractionContext ctx = mock(InteractionContext.class);
		
		NaiveWorkflowStrategyCommand w = new NaiveWorkflowStrategyCommand();
		w.addCommand(command1);
		w.addCommand(command2);
		
		Result result = w.execute(ctx);
		assertEquals(Result.SUCCESS, result);
		verify(command1, times(1)).execute(ctx);
		verify(command2, times(1)).execute(ctx);
	}

	@Test
	public void testResultOfLastCommand() throws InteractionException {
		InteractionCommand command1 = mock(InteractionCommand.class);
		InteractionCommand command2 = mock(InteractionCommand.class);
		when(command2.execute(any(InteractionContext.class))).thenReturn(Result.FAILURE);
		InteractionContext ctx = mock(InteractionContext.class);
		
		NaiveWorkflowStrategyCommand w = new NaiveWorkflowStrategyCommand();
		w.addCommand(command1);
		w.addCommand(command2);
		
		Result result = w.execute(ctx);
		assertEquals(Result.FAILURE, result);
	}

	@Test(expected = AssertionError.class)
	public void testNoCommands() throws InteractionException {
		NaiveWorkflowStrategyCommand w = new NaiveWorkflowStrategyCommand();
		w.execute(mock(InteractionContext.class));
	}

	@Test(expected = AssertionError.class)
	public void testNullContext() throws InteractionException {
		NaiveWorkflowStrategyCommand w = new NaiveWorkflowStrategyCommand();
		w.execute(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullCommands() {
		new NaiveWorkflowStrategyCommand(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNullCommand() {
		new NaiveWorkflowStrategyCommand().addCommand(null);
	}

}
