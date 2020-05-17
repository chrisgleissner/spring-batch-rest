package com.github.chrisgleissner.springbatchrest.util.core.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

@RequiredArgsConstructor
public class RunnableTasklet implements Tasklet {
    private final Runnable runnable;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        runnable.run();
        return FINISHED;
    }
}
