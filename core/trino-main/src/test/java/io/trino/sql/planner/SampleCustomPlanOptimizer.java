/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.sql.planner;

import io.trino.Session;
import io.trino.cost.CostCalculator;
import io.trino.cost.CostComparator;
import io.trino.cost.ScalarStatsCalculator;
import io.trino.cost.StatsCalculator;
import io.trino.cost.TaskCountEstimator;
import io.trino.execution.TaskManagerConfig;
import io.trino.execution.warnings.WarningCollector;
import io.trino.split.PageSourceManager;
import io.trino.split.SplitManager;
import io.trino.sql.PlannerContext;
import io.trino.sql.planner.optimizations.CustomPlanOptimizer;
import io.trino.sql.planner.optimizations.PlanOptimizer;
import io.trino.sql.planner.plan.LimitNode;
import io.trino.sql.planner.plan.PlanNode;
import io.trino.sql.planner.plan.SimplePlanRewriter;

import static java.util.Objects.requireNonNull;

/*
This custom plan optimizer is just added for testing. It's designed to make a simple and easily assertable alteration to the query plan.
It'll will just alter any limit expression from its original value to a new value of 7999.
This will help us test the custom plan optimizer injection feature.
 */

public class SampleCustomPlanOptimizer
        extends CustomPlanOptimizer
{
    public SampleCustomPlanOptimizer()
    {
    }

    @Override
    public PlanOptimizer getPlanOptimizerInstance(PlannerContext plannerContext, TypeAnalyzer typeAnalyzer, TaskManagerConfig taskManagerConfig, boolean forceSingleNode, SplitManager splitManager, PageSourceManager pageSourceManager, StatsCalculator statsCalculator, ScalarStatsCalculator scalarStatsCalculator, CostCalculator costCalculator, CostCalculator estimatedExchangesCostCalculator, CostComparator costComparator, TaskCountEstimator taskCountEstimator, NodePartitioningManager nodePartitioningManager, RuleStatsRecorder ruleStats)
    {
        return new SampleCustomPlanOptimizer();
    }

    @Override
    public PlanNode optimize(PlanNode plan, Session session, TypeProvider types, SymbolAllocator symbolAllocator, PlanNodeIdAllocator idAllocator, WarningCollector warningCollector)
    {
        return SimplePlanRewriter.rewriteWith(new SampleCustomPlanOptimizer.Rewriter(idAllocator), plan);
    }

    private static class Rewriter
            extends SimplePlanRewriter<Void>
    {
        private final PlanNodeIdAllocator idAllocator;

        private Rewriter(PlanNodeIdAllocator idAllocator)
        {
            this.idAllocator = requireNonNull(idAllocator, "idAllocator is null");
        }

        @Override
        public PlanNode visitLimit(LimitNode node, RewriteContext<Void> context)
        {
            LimitNode changedLimitNode = new LimitNode(
                    idAllocator.getNextId(),
                    node.getSource(),
                    7999L,
                    node.isPartial());
            return changedLimitNode;
        }
    }
}
