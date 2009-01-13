def print_todo(dir, path)
  if File.exist?(path)
    puts "== #{dir}"
    puts File.read(path)
    puts
  end
end

Dir["*"].each { |dir| print_todo(dir, File.join(dir, "TODO")) }