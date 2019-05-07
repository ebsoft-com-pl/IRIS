package com.temenos.interaction.core.workflow;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.ArrayList;
import java.util.List;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.command.TransitionCommand;


/**
 * <p>This command implements a workflow that will abort if there is an error.</p>
 * Commands are added to this workflow and then executed in the same order.  If a 
 * command returns an error, the workflow is aborted.
 * @author aphethean
 */
public class AbortOnErrorWorkflowStrategyCommand implements WorkflowCommand {

	protected List<InteractionCommand> commands = new ArrayList<InteractionCommand>();
	protected InteractionCommand lastExecutedCommand = null;

	public AbortOnErrorWorkflowStrategyCommand() {}

	/**
	 * Construct with a list of commands to execute.
	 * @param commands
	 * @invariant commands not null
	 */
	public AbortOnErrorWorkflowStrategyCommand(List<InteractionCommand> commands) {
		this.commands = commands;
		if (commands == null)
			throw new IllegalArgumentException("No commands supplied");		
	}

	@Override
	public boolean isEmpty() {
		return commands.isEmpty();
	}

	@Override
	public ExecutionType getExecutionType() {
		if (lastExecutedCommand instanceof WorkflowCommand) {
			return ((WorkflowCommand)lastExecutedCommand).getExecutionType();
		}
		return lastExecutedCommand instanceof TransitionCommand ? ExecutionType.TRANSITION : ExecutionType.INTERACTION;
	}

	public void addCommand(InteractionCommand command) {
		if (command == null)
			throw new IllegalArgumentException("No command supplied");
		commands.add(command);
	}
	
	/**
	 * @throws InteractionException 
	 * @precondition at least one command has been added {@link addCommand}
	 * @postcondition returned {@link Result) will be the result of a logical 
	 * short-circuit evaluation of the supplied commands.  Short-circut will 
	 * occur when the {@link Command} result is not {@link InteractionCommand.Result.SUCCESS}
	 */
	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {
		assert(commands != null);
		assert(commands.size() > 0) : "There must be at least one command in the workflow";
		if (ctx == null)
			throw new IllegalArgumentException("InteractionContext must be supplied");

		Result result = null;
		for (InteractionCommand command : commands) {
			lastExecutedCommand = command;
			result = command.execute(ctx);
			if (result != Result.SUCCESS)
				break;
		}
		return result;
	}

}
