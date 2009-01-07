Dir["*"].each do |dir|
  path = File.join(dir, "TODO.txt")
  if File.exist?(path)
    puts "== #{dir}"
    puts File.read(path)
    puts
  end
end