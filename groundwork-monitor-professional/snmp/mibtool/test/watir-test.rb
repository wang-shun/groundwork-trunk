#!/opt/local/bin/ruby

require 'rubygems'
require 'firewatir'
include FireWatir

url="http://localhost/play/mib-check"

ff=Firefox.new
ff.goto(url)

File.open("data") do |file|
  while line = file.gets
    ff.text_field(:name, "string_input").set(line.chomp)
    ff.button(:value, "Add").click
  end
end
ff.goto(url)

