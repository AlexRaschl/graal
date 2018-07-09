
ensurePackage <- function(name){
  found <- require(name,character.only = TRUE)
  if(!found){
    install.packages(name)
  }
}
prepareEnv  <- function(){
  ensurePackage("ggplot2")
  ensurePackage("tidyr")
  ensurePackage("plyr")
  ensurePackage("dplyr")
  ensurePackage("reshape2")
  ensurePackage("grid")
  ensurePackage("gridExtra")
  ensurePackage("stringr")
  ensurePackage("jsonlite")
  ensurePackage("tidyverse")
  ensurePackage("magrittr")
  ensurePackage("psych")
  ensurePackage("gtools")
  ensurePackage("ggpubr")
}
prepareEnv()




conference_theme <-
  theme_bw(base_size=20)+
  theme(axis.title.x=element_blank(),axis.title.y=element_blank()) +
  theme(axis.text.x = element_text(angle = 35, hjust = 1)) +
  theme(legend.position="bottom") +
  theme(panel.grid.major.x = element_blank()) +
  theme(
    # facet label
    strip.background = element_rect(colour = NA, fill = NA),
    # remove legend title
    legend.title=element_blank()#,
    #legend.position = "none"
  )+
  theme(panel.spacing = unit(1.33, "lines"))


read_zip <- function(benchzip_postfix,benchmarkNames,configNames,numberOfBenchmarks){
  
  extName <- "benchmarks"
  unzip(paste(extName,".zip",sep=""),exdir = extName)
  
  
  data <- NULL
  for(benchmark in benchmarkNames){
    for(config in configNames) {
      for(i in 1:numberOfBenchmarks){
        nrString <- as.character(i)
        frameFromRead <- data.frame(fromJSON(gzfile(paste("benchmarks/bench_",benchmark,"/","results_",config,"_",nrString,".json",sep=""))))
        # todo fix config for other stuff
        frameFromRead %<>% mutate(queries.host.vm.config=config)
        data <- smartbind(
          data,
            frameFromRead
        )
      }
    }
  }
  if("queries.error" %in% colnames(data)){
    data %<>% filter(is.na(queries.error))
  }
  data %<>% select(one_of(
    "queries.bench.suite",
    "queries.benchmark",
    "queries.host.vm.config",
    "queries.metric.name",
    "queries.metric.object",
    "queries.metric.value",
    "queries.metric.average.over",
    "queries.metric.better"
  ))  
  unlink(extName,recursive = TRUE)
  return(data)
}  

prepareData <-function (){
  
  data <- rbind(
    read_zip("",c("dacapo"),c("default","no_inline"),4) 
  )
  
  # drop all debug metrics execpt graal compile time and installed code size
  data %<>% filter(is.na(queries.metric.object)| queries.metric.object == 'GraalCompiler' | queries.metric.object == 'InstalledCodeSize' | queries.metric.object == 'CompileMemory')
  # drop final time, keep time
  data %<>% filter(queries.metric.name == "compile-time" | queries.metric.name == "time" | queries.metric.name == "count" | queries.metric.name == "throughput" | queries.metric.name == 'allocated-memory')
  # rename metric to object
  data %<>% mutate(queries.metric.name = if_else(is.na(queries.metric.object),queries.metric.name,queries.metric.object))
  # rename metric to short name
  data %<>% mutate(queries.metric.name = if_else(queries.metric.name=="InstalledCodeSize","code size",queries.metric.name))  
  data %<>% mutate(queries.metric.name = if_else(queries.metric.name=="GraalCompiler","compile time",queries.metric.name))
  data %<>% mutate(queries.metric.name = if_else(queries.metric.name=="CompileMemory","compile memory",queries.metric.name))  
  
  
  # drop metric.object
  data %<>% select(-one_of("queries.metric.object","queries.metric.average.over"))
  
  # rename colums
  names(data) <- c("benchsuite","benchmark","config","metric","value","better")
  
  # add baseline column, we take the values of the baseline and compute a geometric mean for them
  data %<>% group_by(benchsuite,benchmark,metric) %>% mutate(metric.baseline=median(value[config=="default"])) %>% ungroup()
  
  # add normalized to baseline column
  data %<>% mutate(value.normalized =value/metric.baseline)
  return(data)
}


presentationColors <- c("#a6d854","#e78ac3","#8da0cb","#fc8d62","#66c2a5")
data1 <- prepareData()
data_dacapo <- data1 %>% filter(benchsuite == "dacapo")

data_dacapo$facet <-
  factor(data_dacapo$metric,
         levels = c("compile time", "code size", "time","compile memory"))

p <-
  ggplot(
    useDingbats = FALSE,
    data = data_dacapo,
    aes(x = benchmark, y = value.normalized, fill = config)
  ) +
  geom_boxplot(
    outlier.colour = "black",
    outlier.shape = 16,
    outlier.size = 2,
    notch = FALSE
  ) +
  scale_y_continuous(labels = scales::percent) +
  scale_fill_manual("VM Configurations", values = presentationColors) +
  geom_hline(yintercept = 1, color = c("black")) +
  conference_theme
p + facet_wrap(~ facet,
               nrow = 3,
               scales = "free_y",
               strip.position = "right")
#ggsave(
#  "dacapo.pdf",
#  dpi = 500,
#  width = 12,
#  height = 8
#)





