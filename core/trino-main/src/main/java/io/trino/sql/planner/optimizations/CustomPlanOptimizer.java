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
package io.trino.sql.planner.optimizations;

import io.airlift.log.Logger;
import io.trino.FeaturesConfig;
import io.trino.cost.*;
import io.trino.execution.TaskManagerConfig;
import io.trino.split.PageSourceManager;
import io.trino.split.SplitManager;
import io.trino.sql.PlannerContext;
import io.trino.sql.planner.NodePartitioningManager;
import io.trino.sql.planner.RuleStatsRecorder;
import io.trino.sql.planner.TypeAnalyzer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CustomPlanOptimizer implements PlanOptimizer {

    @Inject
    private static FeaturesConfig featuresConfig;

    private static final Logger LOG = Logger.get(CustomPlanOptimizer.class);
    public abstract PlanOptimizer getPlanOptimizerInstance(PlannerContext plannerContext,
                                           TypeAnalyzer typeAnalyzer,
                                           TaskManagerConfig taskManagerConfig,
                                           boolean forceSingleNode,
                                           SplitManager splitManager,
                                           PageSourceManager pageSourceManager,
                                           StatsCalculator statsCalculator,
                                           ScalarStatsCalculator scalarStatsCalculator,
                                           CostCalculator costCalculator,
                                           CostCalculator estimatedExchangesCostCalculator,
                                           CostComparator costComparator,
                                           TaskCountEstimator taskCountEstimator,
                                           NodePartitioningManager nodePartitioningManager,
                                           RuleStatsRecorder ruleStats);

    /*
    Reads any injected PlanOptimizer class names from the config and returns a list of their instances
     */
    @Inject
    public static final List<PlanOptimizer> getCustomPlanOptimizers(PlannerContext plannerContext,
                                                        TypeAnalyzer typeAnalyzer,
                                                        TaskManagerConfig taskManagerConfig,
                                                        boolean forceSingleNode,
                                                        SplitManager splitManager,
                                                        PageSourceManager pageSourceManager,
                                                        StatsCalculator statsCalculator,
                                                        ScalarStatsCalculator scalarStatsCalculator,
                                                        CostCalculator costCalculator,
                                                        CostCalculator estimatedExchangesCostCalculator,
                                                        CostComparator costComparator,
                                                        TaskCountEstimator taskCountEstimator,
                                                        NodePartitioningManager nodePartitioningManager,
                                                        RuleStatsRecorder ruleStats
                                                        ){

//        configBinder(binder).bindConfig(FeaturesConfig.class);
        List<PlanOptimizer> listOfPlanOptimizers = new ArrayList<>();
        try {
            //TODO: Read list of custom optimizer classes from config
            String customOptimizer = taskManagerConfig.getAdditionalPlanOptimizerClasses();
//            List<String> customPlanOptimizerClassNames = Arrays.asList("io.trino.sql.planner.SampleCustomPlanOptimizer");
            List<String> customPlanOptimizerClassNames = Arrays.asList(customOptimizer);
            for(String customPlanOptimizerClassName:customPlanOptimizerClassNames){
                //Load the CustomPlanOptimizer instances from the classpath.
                LOG.info("Creating custom optimizer instance %s",customPlanOptimizerClassName);
                CustomPlanOptimizer customPlanOptimizer = (CustomPlanOptimizer) Class.forName(customPlanOptimizerClassName).getDeclaredConstructor().newInstance();
                PlanOptimizer planOptimizer = customPlanOptimizer.getPlanOptimizerInstance(plannerContext,
                        typeAnalyzer,
                        taskManagerConfig,
                        forceSingleNode,
                        splitManager,
                        pageSourceManager,
                        statsCalculator,
                        scalarStatsCalculator,
                        costCalculator,
                        estimatedExchangesCostCalculator,
                        costComparator,
                        taskCountEstimator,
                        nodePartitioningManager,
                        ruleStats);
                listOfPlanOptimizers.add(planOptimizer);
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return  listOfPlanOptimizers;
    }
}
