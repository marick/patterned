# -*- Mode: ruby -*-

require 'rubygems'
require 'rake'

def jar_name
  text = File.read('project.clj')
  unless /patterned\s+"(\d+\.\d+\.\d+(-RC\d+)?)"/ =~ text ||
         /patterned\s+"(\d+\.\d+(\.\d+)*-SNAPSHOT)"/ =~ text || 
         /patterned\s+"(\d+\.\d-alpha\d)"/ =~ text || 
         /patterned\s+"(\d+\.\d\.\d-beta\d)"/ =~ text
    puts "Rake task error: couldn't find version in project file."
    exit 1
  end
  jar = "patterned-#{$1}.jar"
  puts "jar name: #{jar}"
  jar
end

def doit(text)
    puts "== " + text
    system(text)
end

task :default => :fresh

task :fresh do
     doit("lein clean")
     doit("lein jar")
end

task :jar_name do 
  puts jar_name
end

desc "upload to clojars"
task :upload do
  doit("lein pom")
  if File.exist?("patterned.jar")
    doit("mv patterned.jar #{jar_name} ")
  end
  doit("scp pom.xml #{jar_name} clojars@clojars.org:")
end
